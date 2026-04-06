package service

import (
	"errors"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"gorm.io/gorm"
)

type CommentService struct{}

func NewCommentService() *CommentService {
	return &CommentService{}
}

func (s *CommentService) PostComment(userID uint, videoID uint, content string) (*model.Comment, error) {
	if content == "" {
		return nil, errors.New("comment content cannot be empty")
	}

	var comment model.Comment
	err := db.DB.Transaction(func(tx *gorm.DB) error {
		var video model.Video
		if err := tx.First(&video, videoID).Error; err != nil {
			return errors.New("video not found")
		}

		comment = model.Comment{
			UserID:  userID,
			VideoID: videoID,
			Content: content,
		}

		if err := tx.Create(&comment).Error; err != nil {
			return err
		}

		// Update comment count
		if err := tx.Model(&video).Update("comment_count", gorm.Expr("comment_count + ?", 1)).Error; err != nil {
			return err
		}

		// Load user info for response
		if err := tx.Preload("User").First(&comment, comment.ID).Error; err != nil {
			return err
		}

		return nil
	})

	if err != nil {
		return nil, err
	}

	return &comment, nil
}

func (s *CommentService) DeleteComment(userID uint, commentID uint, videoID uint) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		var comment model.Comment
		if err := tx.Where("id = ? AND video_id = ?", commentID, videoID).First(&comment).Error; err != nil {
			return errors.New("comment not found")
		}

		// Check permission (only comment author can delete, optionally video author too but keep it simple here)
		if comment.UserID != userID {
			return errors.New("no permission to delete this comment")
		}

		if err := tx.Delete(&comment).Error; err != nil {
			return err
		}

		// Update comment count
		var video model.Video
		if err := tx.First(&video, videoID).Error; err == nil && video.CommentCount > 0 {
			tx.Model(&video).Update("comment_count", gorm.Expr("comment_count - ?", 1))
		}

		return nil
	})
}

func (s *CommentService) GetCommentList(videoID uint) ([]model.Comment, error) {
	var comments []model.Comment
	if err := db.DB.Preload("User").Where("video_id = ?", videoID).Order("created_at desc").Find(&comments).Error; err != nil {
		return nil, err
	}
	return comments, nil
}
