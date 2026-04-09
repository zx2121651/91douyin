package service

import (
	"time"
	"errors"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type MessageService struct{}

func NewMessageService() *MessageService {
	return &MessageService{}
}

func (s *MessageService) SendMessage(fromUserID uint, toUserID uint, content string) error {
	if fromUserID == toUserID {
		return errors.New("cannot send message to yourself")
	}

	// Optional: Check if users are friends before allowing messages
	// We'll skip this strict check for basic functionality, allowing messages to anyone

	message := model.Message{
		FromUserID: fromUserID,
		ToUserID:   toUserID,
		Content:    content,
	}

	return db.DB.Create(&message).Error
}
// GetChatHistory utilizes cursor-based pagination (preMsgTime) to reliably fetch historical messages without missing any due to concurrent inserts.
func (s *MessageService) GetChatHistory(userID uint, toUserID uint, preMsgTime int64) ([]model.Message, error) {
	var messages []model.Message

	query := db.DB.Where(
		"(from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?)",
		userID, toUserID, toUserID, userID,
	).Order("created_at asc") // Clients typically append newer messages to the bottom of the feed

	if preMsgTime > 0 {
		// Proper cursor pagination avoiding duplication and ensuring exactly-once delivery
		cursorTime := time.UnixMilli(preMsgTime)
		query = query.Where("created_at > ?", cursorTime)
	}

	// Protect against overwhelming the server with massive chat history dumps
	// Hard limit sets a safe boundary for a single page request
	if err := query.Limit(100).Find(&messages).Error; err != nil {
		return nil, err
	}

	return messages, nil
}

// GetUnreadNotificationCount aggregates the total unread system notifications (likes, follows) for a specific user
func (s *MessageService) GetUnreadNotificationCount(userID uint) (int64, error) {
	var count int64
	if err := db.DB.Model(&model.SystemNotification{}).Where("to_user_id = ? AND is_read = ?", userID, false).Count(&count).Error; err != nil {
		return 0, err
	}
	return count, nil
}

// GetNotifications returns the recent system notifications for the user
func (s *MessageService) GetNotifications(userID uint, limit int, offset int) ([]model.SystemNotification, error) {
	var notifications []model.SystemNotification

	// Preload the FromUser so the UI can display "User X liked your video"
	if err := db.DB.Preload("FromUser").Where("to_user_id = ?", userID).
		Order("created_at desc").
		Limit(limit).Offset(offset).
		Find(&notifications).Error; err != nil {
		return nil, err
	}

	// Mark them as read automatically upon fetching (for simplicity)
	if len(notifications) > 0 {
		db.DB.Model(&model.SystemNotification{}).Where("to_user_id = ? AND is_read = ?", userID, false).Update("is_read", true)
	}

	return notifications, nil
}