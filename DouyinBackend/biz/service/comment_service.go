package service

import (
	"errors"
	"strings"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"gorm.io/gorm"
)

type CommentService struct{}

func NewCommentService() *CommentService {
	return &CommentService{}
}

// PostComment supports creating both top-level comments and nested replies
func (s *CommentService) PostComment(userID uint, videoID uint, content string, parentID *uint, idempotencyKey string) (*model.Comment, error) {
	content = strings.TrimSpace(content)
	if content == "" {
		return nil, errors.New("comment content cannot be empty")
	}

	var keyPtr *string
	if idempotencyKey != "" {
		keyPtr = &idempotencyKey
	}

	var comment model.Comment
	err := db.DB.Transaction(func(tx *gorm.DB) error {
		// Check for idempotency if key is provided
		if keyPtr != nil {
			var existing model.Comment
			if err := tx.Where("idempotency_key = ?", *keyPtr).Preload("User").First(&existing).Error; err == nil {
				comment = existing
				return nil
			} else if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}
		}

		var video model.Video
		if err := tx.First(&video, videoID).Error; err != nil {
			return errors.New("video not found")
		}

		comment = model.Comment{
			UserID:         userID,
			VideoID:        videoID,
			Content:        content,
			IdempotencyKey: keyPtr,
		}

		// Handle hierarchical validation and reply count
		if parentID != nil && *parentID > 0 {
			var parentComment model.Comment
			// Ensure the parent exists and belongs to the same video
			if err := tx.Where("id = ? AND video_id = ?", *parentID, videoID).First(&parentComment).Error; err != nil {
				return errors.New("parent comment not found or invalid")
			}
			comment.ParentID = parentID

			// Increment reply count of parent comment
			if err := tx.Model(&model.Comment{}).Where("id = ?", *parentID).Update("reply_count", gorm.Expr("reply_count + ?", 1)).Error; err != nil {
				return err
			}
		}

		if err := tx.Create(&comment).Error; err != nil {
			return err
		}

		// Update total video comment count
		if err := tx.Model(&video).Update("comment_count", gorm.Expr("comment_count + ?", 1)).Error; err != nil {
			return err
		}

		// Load user info for the API response
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
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil // Idempotent: already deleted
			}
			return err
		}

		// Check permission (only comment author can delete)
		if comment.UserID != userID {
			return errors.New("no permission to delete this comment")
		}

		// Delete the comment itself and optionally cascade delete replies (if implemented via DB foreign key cascading)
		// For simplicity, we just delete the current comment.
		if err := tx.Delete(&comment).Error; err != nil {
			return err
		}

		// If this was a reply to another comment, decrement the parent's reply_count
		if comment.ParentID != nil && *comment.ParentID > 0 {
			tx.Model(&model.Comment{}).Where("id = ? AND reply_count > 0", *comment.ParentID).Update("reply_count", gorm.Expr("reply_count - ?", 1))
		}

		// Decrement video comment count safely (avoid negative counts)
		tx.Model(&model.Video{}).Where("id = ? AND comment_count > 0", videoID).Update("comment_count", gorm.Expr("comment_count - ?", 1))

		return nil
	})
}

// GetCommentList retrieves top-level comments along with their latest replies eagerly loaded
func (s *CommentService) GetCommentList(videoID uint) ([]model.Comment, error) {
	var topLevelComments []model.Comment

	// Fetch only top-level comments (ParentID is null), preloading their User and Replies
	// In GORM, Preload("Replies") handles the 1-to-many relationship.
	// We also Preload("Replies.User") to get the author of the replies.
	err := db.DB.
		Preload("User").
		Preload("Replies", func(db *gorm.DB) *gorm.DB {
			return db.Order("created_at asc").Limit(3) // Only fetch top 3 replies per comment for the feed view
		}).
		Preload("Replies.User").
		Where("video_id = ? AND parent_id IS NULL", videoID).
		Order("created_at desc").
		Find(&topLevelComments).Error

	if err != nil {
		return nil, err
	}
	return topLevelComments, nil
}
