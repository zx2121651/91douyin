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

	baseQuery := db.DB.Where(
		"(from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?)",
		userID, toUserID, toUserID, userID,
	)

	if preMsgTime > 0 {
		// Fetch new messages since preMsgTime in chronological order
		cursorTime := time.UnixMilli(preMsgTime)
		if err := baseQuery.Where("created_at > ?", cursorTime).Order("created_at asc").Limit(100).Find(&messages).Error; err != nil {
			return nil, err
		}
	} else {
		// Initial fetch: get latest 100 messages in reverse chronological order
		if err := baseQuery.Order("created_at desc").Limit(100).Find(&messages).Error; err != nil {
			return nil, err
		}
		// Reverse to chronological order (Ascending) for client display
		for i, j := 0, len(messages)-1; i < j; i, j = i+1, j-1 {
			messages[i], messages[j] = messages[j], messages[i]
		}
	}

	// Mark messages from toUserID to userID as read
	if len(messages) > 0 {
		db.DB.Model(&model.Message{}).
			Where("from_user_id = ? AND to_user_id = ? AND is_read = ?", toUserID, userID, false).
			Update("is_read", true)
	}

	return messages, nil
}

// GetTotalUnreadCount aggregates unread private messages and system notifications
func (s *MessageService) GetTotalUnreadCount(userID uint) (int64, error) {
	var msgCount int64
	if err := db.DB.Model(&model.Message{}).Where("to_user_id = ? AND is_read = ?", userID, false).Count(&msgCount).Error; err != nil {
		return 0, err
	}

	var notifyCount int64
	if err := db.DB.Model(&model.SystemNotification{}).Where("to_user_id = ? AND is_read = ?", userID, false).Count(&notifyCount).Error; err != nil {
		return msgCount, nil
	}

	return msgCount + notifyCount, nil
}

type Conversation struct {
	User        model.User
	LastMessage model.Message
	UnreadCount int64
}

func (s *MessageService) GetConversationList(userID uint) ([]Conversation, error) {
	var conversations []Conversation

	// Find all unique users interacted with
	var userIDs []uint
	db.DB.Model(&model.Message{}).
		Select("DISTINCT CASE WHEN from_user_id = ? THEN to_user_id ELSE from_user_id END", userID).
		Where("from_user_id = ? OR to_user_id = ?", userID, userID).
		Find(&userIDs)

	for _, otherID := range userIDs {
		var lastMsg model.Message
		db.DB.Where("(from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?)",
			userID, otherID, otherID, userID).
			Order("created_at desc").First(&lastMsg)

		var unreadCount int64
		db.DB.Model(&model.Message{}).
			Where("from_user_id = ? AND to_user_id = ? AND is_read = ?", otherID, userID, false).
			Count(&unreadCount)

		var otherUser model.User
		db.DB.First(&otherUser, otherID)

		conversations = append(conversations, Conversation{
			User:        otherUser,
			LastMessage: lastMsg,
			UnreadCount: unreadCount,
		})
	}

	return conversations, nil
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