package service

import (
	"errors"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"gorm.io/gorm"
)

type FavoriteService struct{}

func NewFavoriteService() *FavoriteService {
	return &FavoriteService{}
}

func (s *FavoriteService) FavoriteAction(userID uint, videoID uint, actionType int32) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		var video model.Video
		if err := tx.First(&video, videoID).Error; err != nil {
			return errors.New("video not found")
		}

		favorite := model.Favorite{
			UserID:  userID,
			VideoID: videoID,
		}

		if actionType == 1 { // Like
			// Check if already liked
			err := tx.Where("user_id = ? AND video_id = ?", userID, videoID).First(&model.Favorite{}).Error
			if err == nil {
				return errors.New("already liked")
			} else if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}

			if err := tx.Create(&favorite).Error; err != nil {
				return err
			}

			// Update video favorite count
			if err := tx.Model(&video).Update("favorite_count", gorm.Expr("favorite_count + ?", 1)).Error; err != nil {
				return err
			}
		} else if actionType == 2 { // Unlike
			res := tx.Unscoped().Where("user_id = ? AND video_id = ?", userID, videoID).Delete(&model.Favorite{})
			if res.Error != nil {
				return res.Error
			}
			if res.RowsAffected > 0 {
				// Prevent negative count
				if video.FavoriteCount > 0 {
					if err := tx.Model(&video).Update("favorite_count", gorm.Expr("favorite_count - ?", 1)).Error; err != nil {
						return err
					}
				}
			} else {
				return errors.New("not liked yet")
			}
		} else {
			return errors.New("invalid action type")
		}

		return nil
	})
}

func (s *FavoriteService) GetFavoriteList(userID uint) ([]model.Video, error) {
	var favorites []model.Favorite
	if err := db.DB.Where("user_id = ?", userID).Find(&favorites).Error; err != nil {
		return nil, err
	}

	if len(favorites) == 0 {
		return []model.Video{}, nil
	}

	var videoIDs []uint
	for _, fav := range favorites {
		videoIDs = append(videoIDs, fav.VideoID)
	}

	var videos []model.Video
	if err := db.DB.Preload("Author").Where("id IN ?", videoIDs).Order("created_at desc").Find(&videos).Error; err != nil {
		return nil, err
	}

	return videos, nil
}

// IsFavorite checks if a user has favorited a video
func (s *FavoriteService) IsFavorite(userID uint, videoID uint) bool {
	if userID == 0 {
		return false
	}
	var count int64
	db.DB.Model(&model.Favorite{}).Where("user_id = ? AND video_id = ?", userID, videoID).Count(&count)
	return count > 0
}

// IsFavoriteMap performs a bulk lookup to check if a user has favorited a set of videos (solves N+1 query problem)
func (s *FavoriteService) IsFavoriteMap(userID uint, videoIDs []uint) (map[uint]bool, error) {
	resultMap := make(map[uint]bool)
	if userID == 0 || len(videoIDs) == 0 {
		for _, id := range videoIDs {
			resultMap[id] = false
		}
		return resultMap, nil
	}

	var favorites []model.Favorite
	if err := db.DB.Where("user_id = ? AND video_id IN ?", userID, videoIDs).Find(&favorites).Error; err != nil {
		return nil, err
	}

	for _, fav := range favorites {
		resultMap[fav.VideoID] = true
	}

	for _, id := range videoIDs {
		if _, exists := resultMap[id]; !exists {
			resultMap[id] = false
		}
	}

	return resultMap, nil
}
