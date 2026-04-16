package service

import (
	"sync"
	"testing"

	"github.com/douyin/backend/biz/common/config"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/stretchr/testify/assert"
)

func setupTestDBInteraction() {
	config.GlobalConfig = &config.Config{
		Database: config.DatabaseConfig{
			DSN: "file:interaction_test.db?cache=shared&mode=memory",
		},
	}
	db.Init()
}

func TestInteractionConsistency(t *testing.T) {
	setupTestDBInteraction()

	favoriteService := NewFavoriteService()
	relationService := NewRelationService()
	commentService := NewCommentService()

	// Setup users and video
	user1 := model.User{Username: "user1", Name: "user1"}
	user2 := model.User{Username: "user2", Name: "user2"}
	if err := db.DB.Create(&user1).Error; err != nil {
		t.Fatalf("failed to create user1: %v", err)
	}
	if err := db.DB.Create(&user2).Error; err != nil {
		t.Fatalf("failed to create user2: %v", err)
	}

	video := model.Video{AuthorID: user1.ID, Title: "test video"}
	if err := db.DB.Create(&video).Error; err != nil {
		t.Fatalf("failed to create video: %v", err)
	}

	t.Run("Favorite Idempotency", func(t *testing.T) {
		// Like twice
		err := favoriteService.FavoriteAction(user2.ID, video.ID, 1)
		assert.NoError(t, err)

		err = favoriteService.FavoriteAction(user2.ID, video.ID, 1)
		// Should be idempotent
		assert.NoError(t, err)

		var v model.Video
		db.DB.First(&v, video.ID)
		assert.Equal(t, int64(1), v.FavoriteCount)

		// Unlike twice
		err = favoriteService.FavoriteAction(user2.ID, video.ID, 2)
		assert.NoError(t, err)

		err = favoriteService.FavoriteAction(user2.ID, video.ID, 2)
		// Should be idempotent
		assert.NoError(t, err)

		db.DB.First(&v, video.ID)
		assert.Equal(t, int64(0), v.FavoriteCount)
	})

	t.Run("Relation Idempotency", func(t *testing.T) {
		// Follow twice
		err := relationService.RelationAction(user2.ID, user1.ID, 1)
		assert.NoError(t, err)

		err = relationService.RelationAction(user2.ID, user1.ID, 1)
		assert.NoError(t, err)

		var u1, u2 model.User
		db.DB.First(&u1, user1.ID)
		db.DB.First(&u2, user2.ID)
		assert.Equal(t, int64(1), u1.FollowerCount)
		assert.Equal(t, int64(1), u2.FollowCount)

		// Unfollow twice
		err = relationService.RelationAction(user2.ID, user1.ID, 2)
		assert.NoError(t, err)

		err = relationService.RelationAction(user2.ID, user1.ID, 2)
		assert.NoError(t, err)

		db.DB.First(&u1, user1.ID)
		db.DB.First(&u2, user2.ID)
		assert.Equal(t, int64(0), u1.FollowerCount)
		assert.Equal(t, int64(0), u2.FollowCount)
	})

	t.Run("Concurrent Favorite", func(t *testing.T) {
		const concurrentCount = 5
		var wg sync.WaitGroup
		wg.Add(concurrentCount)

		for i := 0; i < concurrentCount; i++ {
			go func() {
				defer wg.Done()
				_ = favoriteService.FavoriteAction(user2.ID, video.ID, 1)
			}()
		}
		wg.Wait()

		var v model.Video
		db.DB.First(&v, video.ID)
		// If idempotent, count should be 1.
		assert.Equal(t, int64(1), v.FavoriteCount)
	})

	t.Run("Comment Counter Consistency", func(t *testing.T) {
		// Post a comment
		comment, err := commentService.PostComment(user2.ID, video.ID, "test comment", nil, "")
		assert.NoError(t, err)

		var v model.Video
		db.DB.First(&v, video.ID)
		assert.Equal(t, int64(1), v.CommentCount)

		// Delete same comment twice
		err = commentService.DeleteComment(user2.ID, comment.ID, video.ID)
		assert.NoError(t, err)

		err = commentService.DeleteComment(user2.ID, comment.ID, video.ID)
		// Should be idempotent
		assert.NoError(t, err)

		db.DB.First(&v, video.ID)
		assert.Equal(t, int64(0), v.CommentCount)
	})

	t.Run("Comment Idempotency Key", func(t *testing.T) {
		const key = "idempotency-key-1"
		// Post with key
		c1, err := commentService.PostComment(user2.ID, video.ID, "test comment", nil, key)
		assert.NoError(t, err)

		// Post with same key
		c2, err := commentService.PostComment(user2.ID, video.ID, "test comment", nil, key)
		assert.NoError(t, err)

		assert.Equal(t, c1.ID, c2.ID)

		var count int64
		db.DB.Model(&model.Comment{}).Where("video_id = ?", video.ID).Count(&count)
	})

	t.Run("Multiple Comments Without Keys", func(t *testing.T) {
		// Post multiple comments without keys
		c1, err := commentService.PostComment(user2.ID, video.ID, "comment 1", nil, "")
		assert.NoError(t, err)

		c2, err := commentService.PostComment(user2.ID, video.ID, "comment 2", nil, "")
		assert.NoError(t, err)

		assert.NotEqual(t, c1.ID, c2.ID)
	})
}
