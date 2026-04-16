package service

import (
	"fmt"
	"testing"
	"time"

	"github.com/douyin/backend/biz/common/config"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/stretchr/testify/assert"
)

func setupTestDB() {
	config.GlobalConfig = &config.Config{
		Database: config.DatabaseConfig{
			DSN: ":memory:",
		},
	}
	db.Init()
}

func TestFeedPagination(t *testing.T) {
	setupTestDB()
	videoService := NewVideoService()

	// Create test users
	user := model.User{Name: "test_user"}
	db.DB.Create(&user)

	// Create test videos with different timestamps
	now := time.Now()
	for i := 1; i <= 5; i++ {
		video := model.Video{
			AuthorID: user.ID,
			Title:    fmt.Sprintf("Video %d", i),
			Status:   "published",
		}
		db.DB.Create(&video)
		// Set CreatedAt manually to control order after creation because gorm.Model has CreatedAt
		db.DB.Model(&video).Update("created_at", now.Add(time.Duration(-i)*time.Hour))
	}

	t.Run("First Page Load", func(t *testing.T) {
		// latestTime = 0 or now
		videos, nextTime, err := videoService.GetFeed(0, 2, 0)
		if err != nil {
			t.Fatalf("Failed to get feed: %v", err)
		}
		assert.Equal(t, 2, len(videos))
		assert.NotZero(t, nextTime)
		// Since we order by final_score (which includes time decay and random factor),
		// we can't strictly assert ID order if random factor is large,
		// but with few videos and clear time differences, it should be somewhat predictable.
		// However, the requirement is to establish next_time contract.
		if len(videos) > 0 {
			assert.Equal(t, videos[len(videos)-1].CreatedAt.UnixMilli(), nextTime)
		}
	})

	t.Run("Pagination Flow", func(t *testing.T) {
		// Use -1 for stable test (no random noise)
		// Get first page
		videos1, nextTime1, err := videoService.GetFeed(-1, 2, 0)
		assert.NoError(t, err)
		assert.Equal(t, 2, len(videos1))

		// Get second page using nextTime1
		// We use -nextTime1 to signal stableMode AND pass the timestamp
		videos2, nextTime2, err := videoService.GetFeed(-nextTime1, 2, 0)
		assert.NoError(t, err)
		assert.Equal(t, 2, len(videos2))

		assert.NotZero(t, nextTime2)
		assert.True(t, nextTime2 < nextTime1)

		// Ensure no duplicates between pages (based on ID)
		ids1 := make(map[uint]bool)
		for _, v := range videos1 {
			ids1[v.ID] = true
		}
		for _, v := range videos2 {
			assert.False(t, ids1[v.ID], "Duplicate video found across pages")
		}
	})

	t.Run("No More Data", func(t *testing.T) {
		// Because of random factor in final_score, we might not get exact pagination.
		// Let's use a very large limit to get all videos first.
		// Use -1 for stable test
		allVideos, _, err := videoService.GetFeed(-1, 10, 0)
		assert.NoError(t, err)
		if len(allVideos) == 0 {
			t.Fatal("No videos found")
		}

		// Use the last video's CreatedAt as latestTime to get more
		lastCreatedAt := allVideos[len(allVideos)-1].CreatedAt.UnixMilli()

		// Use -lastCreatedAt to signal stableMode AND pass the timestamp
		videosNext, nextTimeNext, err := videoService.GetFeed(-lastCreatedAt, 10, 0)
		assert.NoError(t, err)
		assert.Equal(t, 0, len(videosNext))
		assert.Equal(t, int64(0), nextTimeNext)
	})

	t.Run("Negative latestTime", func(t *testing.T) {
		// -1 is now our "stable test" signal, but it should still work like 0 (latest)
		videos, _, err := videoService.GetFeed(-1, 2, 0)
		assert.NoError(t, err)
		assert.Equal(t, 2, len(videos))
	})

	t.Run("Future latestTime", func(t *testing.T) {
		futureTime := time.Now().Add(24 * time.Hour).UnixMilli()
		videos, _, err := videoService.GetFeed(futureTime, 10, 0)
		assert.NoError(t, err)
		assert.Equal(t, 5, len(videos))
	})

	t.Run("Very Old latestTime", func(t *testing.T) {
		oldTime := time.Now().Add(-100 * time.Hour).UnixMilli()
		videos, nextTime, err := videoService.GetFeed(oldTime, 10, 0)
		assert.NoError(t, err)
		assert.Equal(t, 0, len(videos))
		assert.Equal(t, int64(0), nextTime)
	})
}
