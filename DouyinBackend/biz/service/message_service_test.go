package service

import (
	"testing"
	"time"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/stretchr/testify/assert"
)

func TestMessageService_GetChatHistory(t *testing.T) {
	setupTestDBInteraction()

	messageService := NewMessageService()

	user1 := model.User{Username: "sender", Name: "sender"}
	user2 := model.User{Username: "receiver", Name: "receiver"}
	db.DB.Create(&user1)
	db.DB.Create(&user2)

	// Create 5 messages with clear time gaps
	for i := 1; i <= 5; i++ {
		msg := model.Message{
			FromUserID: user1.ID,
			ToUserID:   user2.ID,
			Content:    "message",
		}
		db.DB.Create(&msg)
		// Larger sleep to ensure distinct created_at and avoid precision issues
		time.Sleep(100 * time.Millisecond)
	}

	t.Run("Initial fetch (preMsgTime = 0)", func(t *testing.T) {
		messages, err := messageService.GetChatHistory(user1.ID, user2.ID, 0)
		assert.NoError(t, err)
		assert.Equal(t, 5, len(messages))

		// Check chronological order (Ascending)
		for i := 0; i < len(messages)-1; i++ {
			assert.True(t, messages[i].CreatedAt.Before(messages[i+1].CreatedAt))
		}
	})

	t.Run("Polling fetch (preMsgTime > 0)", func(t *testing.T) {
		messages, err := messageService.GetChatHistory(user1.ID, user2.ID, 0)
		assert.NoError(t, err)

		// Use a timestamp that is between messages[2] and messages[3]
		// Since we have 100ms gap, adding 50ms is safe.
		lastMsgTime := messages[2].CreatedAt.UnixMilli() + 50

		newMessages, err := messageService.GetChatHistory(user1.ID, user2.ID, lastMsgTime)
		assert.NoError(t, err)
		assert.Equal(t, 2, len(newMessages))
		assert.Equal(t, messages[3].ID, newMessages[0].ID)
		assert.Equal(t, messages[4].ID, newMessages[1].ID)
	})

	t.Run("Read recovery in GetChatHistory", func(t *testing.T) {
		// user1 sent messages to user2. They should be unread for user2 initially.
		var count int64
		db.DB.Model(&model.Message{}).Where("to_user_id = ? AND is_read = ?", user2.ID, false).Count(&count)
		assert.Equal(t, int64(5), count)

		// user2 fetches chat history. Messages should be marked as read.
		_, err := messageService.GetChatHistory(user2.ID, user1.ID, 0)
		assert.NoError(t, err)

		db.DB.Model(&model.Message{}).Where("to_user_id = ? AND is_read = ?", user2.ID, false).Count(&count)
		assert.Equal(t, int64(0), count)
	})

	t.Run("GetTotalUnreadCount", func(t *testing.T) {
		// Clear existing notifications/messages if any
		db.DB.Exec("DELETE FROM messages")
		db.DB.Exec("DELETE FROM system_notifications")

		// 3 unread messages
		for i := 0; i < 3; i++ {
			db.DB.Create(&model.Message{ToUserID: user1.ID, FromUserID: user2.ID, Content: "unread", IsRead: false})
		}
		// 2 unread notifications
		for i := 0; i < 2; i++ {
			db.DB.Create(&model.SystemNotification{ToUserID: user1.ID, Type: "like", IsRead: false})
		}

		count, err := messageService.GetTotalUnreadCount(user1.ID)
		assert.NoError(t, err)
		assert.Equal(t, int64(5), count)
	})
}
