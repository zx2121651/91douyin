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

func (s *MessageService) GetChatHistory(userID uint, toUserID uint, preMsgTime int64) ([]model.Message, error) {
	var messages []model.Message

	query := db.DB.Where(
		"(from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?)",
		userID, toUserID, toUserID, userID,
	).Order("created_at asc") // API typically expects chronological order

	// Only return messages newer than preMsgTime if provided
	if preMsgTime > 0 {
		query = query.Where("created_at > ?", time.UnixMilli(preMsgTime))
	}

	if err := query.Find(&messages).Error; err != nil {
		return nil, err
	}

	return messages, nil
}
