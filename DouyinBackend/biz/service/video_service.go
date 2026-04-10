package service

import (
	"time"
	"gorm.io/gorm"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type VideoService struct{}

func NewVideoService() *VideoService {
	return &VideoService{}
}

func (s *VideoService) GetFeed(latestTime int64, limit int, currentUserID uint) ([]model.Video, int64, error) {
	var videos []model.Video

	// Recommendation System 2.0 (User-Item Collaborative Filtering concept)
	// If a user is logged in, we fetch their top affinity tags and boost matching videos
	var userTags []model.UserTag
	if currentUserID > 0 {
		db.DB.Where("user_id = ?", currentUserID).Order("affinity_score desc").Limit(3).Find(&userTags)
	}

	if len(userTags) > 0 {
		// Custom SQL to combine Hot Rank Decay with User Affinity Personalization
		// Score = Hotness + (UserTag Affinity * 10)
		tagStr1 := userTags[0].CategoryTag
		tagScore1 := userTags[0].AffinityScore

		query := `
			SELECT v.*,
			(
				((v.favorite_count * 2.0 + v.comment_count * 1.0) / POW((julianday('now') - julianday(v.created_at)) + 1.0, 1.5))
				+ CASE WHEN v.category_tag = ? THEN ? ELSE 0 END
			) as final_score
			FROM videos v
			WHERE v.status = 'published' AND v.deleted_at IS NULL
		`
		args := []interface{}{tagStr1, tagScore1 * 10.0}

		if latestTime > 0 {
			query += " AND v.created_at < ?"
			args = append(args, time.UnixMilli(latestTime))
		}

		query += " ORDER BY final_score DESC LIMIT ?"
		args = append(args, limit)

		if err := db.DB.Raw(query, args...).Scan(&videos).Error; err != nil {
			return nil, 0, err
		}

		// Preload authors manually for Raw scan
		var authorIDs []uint
		for _, v := range videos {
			authorIDs = append(authorIDs, v.AuthorID)
		}
		if len(authorIDs) > 0 {
			var authors []model.User
			if err := db.DB.Where("id IN ?", authorIDs).Find(&authors).Error; err == nil {
				authorMap := make(map[uint]model.User)
				for _, a := range authors {
					authorMap[a.ID] = a
				}
				for i, v := range videos {
					videos[i].Author = authorMap[v.AuthorID]
				}
			}
		}

	} else {
		// Fallback to chronological feed if user has no data or is a guest
		query := db.DB.Preload("Author").Where("status = ?", "published").Order("created_at desc").Limit(limit)
		if latestTime > 0 {
			query = query.Where("created_at < ?", time.UnixMilli(latestTime))
		}
		if err := query.Find(&videos).Error; err != nil {
			return nil, 0, err
		}
	}

	var nextTime int64
	if len(videos) > 0 {
		nextTime = videos[len(videos)-1].CreatedAt.UnixMilli()
	} else {
		nextTime = time.Now().UnixMilli()
	}

	return videos, nextTime, nil
}

func (s *VideoService) PublishVideo(authorID uint, title string, playURL string, coverURL string) error {
	// Step 1: Immediately persist the video as "processing" to ensure the user gets a fast response
	video := model.Video{
		AuthorID:      authorID,
		PlayURL:       playURL,
		CoverURL:      coverURL,
		Title:         title,
		FavoriteCount: 0,
		CommentCount:  0,
		Status:        "processing",
		CategoryTag:   "comedy", // In reality, an ML model would auto-classify this
	}

	if err := db.DB.Create(&video).Error; err != nil {
		return err
	}

	// Step 2: Spin off a Goroutine to handle asynchronous media post-processing and checks
	go func(vid uint, uid uint) {
		// Simulate network extraction, FFmpeg transcoding delay, and content safety moderation
		time.Sleep(3 * time.Second)

		// After processing, update the status to "published" safely inside a transaction
		db.DB.Transaction(func(tx *gorm.DB) error {
			if err := tx.Model(&model.Video{}).Where("id = ?", vid).Update("status", "published").Error; err != nil {
				return err
			}

			// Generate a system notification to the author confirming successful processing
			notif := model.SystemNotification{
				ToUserID:   uid,
				Type:       model.NotificationTypeSystem,
				Content:    "您的视频《" + title + "》已发布成功",
				TargetID:   vid,
			}
			tx.Create(&notif)

			return nil
		})
	}(video.ID, authorID)

	return nil
}

func (s *VideoService) GetPublishList(userID uint) ([]model.Video, error) {
	var videos []model.Video

	if err := db.DB.Preload("Author").Where("author_id = ? AND status = ?", userID, "published").Order("created_at desc").Find(&videos).Error; err != nil {
		return nil, err
	}

	return videos, nil
}
