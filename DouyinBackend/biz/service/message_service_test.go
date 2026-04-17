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
}
