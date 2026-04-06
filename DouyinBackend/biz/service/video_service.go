package service

import (
	"time"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type VideoService struct{}

func NewVideoService() *VideoService {
	return &VideoService{}
}

func (s *VideoService) GetFeed(latestTime int64, limit int) ([]model.Video, int64, error) {
	var videos []model.Video

	query := db.DB.Preload("Author").Order("created_at desc").Limit(limit)

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
	video := model.Video{
		AuthorID:      authorID,
		PlayURL:       playURL,
		CoverURL:      coverURL,
		Title:         title,
		FavoriteCount: 0,
		CommentCount:  0,
	}

	return db.DB.Create(&video).Error
}

func (s *VideoService) GetPublishList(userID uint) ([]model.Video, error) {
	var videos []model.Video

	if err := db.DB.Preload("Author").Where("author_id = ?", userID).Order("created_at desc").Find(&videos).Error; err != nil {
		return nil, err
	}

	return videos, nil
}
