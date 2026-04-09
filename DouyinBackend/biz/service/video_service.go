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

func (s *VideoService) GetFeed(latestTime int64, limit int) ([]model.Video, int64, error) {
	var videos []model.Video

	query := db.DB.Preload("Author").Where("status = ?", "published").Order("created_at desc").Limit(limit)

	if latestTime > 0 {
		query = query.Where("created_at < ?", time.UnixMilli(latestTime))
	}

	if err := query.Find(&videos).Error; err != nil {
		return nil, 0, err
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
