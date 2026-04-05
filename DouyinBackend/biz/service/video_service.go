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
