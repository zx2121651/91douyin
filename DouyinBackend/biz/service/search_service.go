package service

import (
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type SearchService struct{}

func NewSearchService() *SearchService {
	return &SearchService{}
}

// SearchVideos searches for videos containing the keyword in title
func (s *SearchService) SearchVideos(keyword string, offset int, limit int) ([]model.Video, error) {
	var videos []model.Video

	// Basic LIKE query. In production, this would be ElasticSearch or Meilisearch
	if err := db.DB.Preload("Author").Where("title LIKE ?", "%"+keyword+"%").
		Order("favorite_count desc, created_at desc").
		Offset(offset).Limit(limit).Find(&videos).Error; err != nil {
		return nil, err
	}

	return videos, nil
}

// SearchUsers searches for users containing the keyword in name or username
func (s *SearchService) SearchUsers(keyword string, offset int, limit int) ([]model.User, error) {
	var users []model.User

	if err := db.DB.Where("name LIKE ? OR username LIKE ?", "%"+keyword+"%", "%"+keyword+"%").
		Order("follower_count desc, created_at desc").
		Offset(offset).Limit(limit).Find(&users).Error; err != nil {
		return nil, err
	}

	return users, nil
}
