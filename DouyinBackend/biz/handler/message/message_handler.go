package message

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
	"github.com/douyin/backend/biz/model/common"
	message_model "github.com/douyin/backend/biz/model/message"
	"github.com/douyin/backend/biz/service"
)

var messageService = service.NewMessageService()

func MessageAction(ctx context.Context, c *app.RequestContext) {
	var req message_model.MessageActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		utils.SendResponse(c, errno.AuthErr, nil)
		return
	}
	userID := userIDRaw.(uint)

	err := messageService.SendMessage(userID, uint(req.ToUserID), req.Content)
	if err != nil {
		utils.SendResponse(c, err, nil)
		return
	}

	utils.SendResponse(c, errno.Success, nil)
}

func MessageChat(ctx context.Context, c *app.RequestContext) {
	var req message_model.MessageChatRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		utils.SendResponse(c, errno.AuthErr, nil)
		return
	}
	userID := userIDRaw.(uint)

	messages, err := messageService.GetChatHistory(userID, uint(req.ToUserID), req.PreMsgTime)
	if err != nil {
		utils.SendResponse(c, err, nil)
		return
	}

	var messageList []message_model.Message
	for _, m := range messages {
		messageList = append(messageList, message_model.Message{
			ID:         int64(m.ID),
			ToUserID:   int64(m.ToUserID),
			FromUserID: int64(m.FromUserID),
			Content:    m.Content,
			CreateTime: m.CreatedAt.UnixMilli(),
		})
	}
    // Handle empty slice serialization to empty array instead of null
	if messageList == nil {
		messageList = []message_model.Message{}
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"message_list": messageList,
	})
}

func MessageActionList(ctx context.Context, c *app.RequestContext) {
	userIDRaw, exists := c.Get("user_id")
	if !exists {
		utils.SendResponse(c, errno.AuthErr, nil)
		return
	}
	userID := userIDRaw.(uint)

	conversations, err := messageService.GetConversationList(userID)
	if err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage(err.Error()), nil)
		return
	}

	type ConversationDTO struct {
		User        *common.User `json:"user"`
		LastMessage string       `json:"last_message"`
		CreateTime  int64        `json:"create_time"`
		UnreadCount int64        `json:"unread_count"`
	}

	var conversationList []ConversationDTO
	for _, conv := range conversations {
		conversationList = append(conversationList, ConversationDTO{
			User: &common.User{
				ID:            int64(conv.User.ID),
				Name:          conv.User.Name,
				FollowCount:   conv.User.FollowCount,
				FollowerCount: conv.User.FollowerCount,
				Avatar:        conv.User.Avatar,
			},
			LastMessage: conv.LastMessage.Content,
			CreateTime:  conv.LastMessage.CreatedAt.UnixMilli(),
			UnreadCount: conv.UnreadCount,
		})
	}

	if conversationList == nil {
		conversationList = []ConversationDTO{}
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"conversation_list": conversationList,
	})
}

func UnreadCount(ctx context.Context, c *app.RequestContext) {
	userIDRaw, exists := c.Get("user_id")
	if !exists {
		utils.SendResponse(c, errno.AuthErr, nil)
		return
	}
	userID := userIDRaw.(uint)

	count, err := messageService.GetTotalUnreadCount(userID)
	if err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage(err.Error()), nil)
		return
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"unread_count": count,
	})
}

func GetNotifications(ctx context.Context, c *app.RequestContext) {
	userIDRaw, exists := c.Get("user_id")
	if !exists {
		utils.SendResponse(c, errno.AuthErr, nil)
		return
	}
	userID := userIDRaw.(uint)

	// Simple pagination
	notifications, err := messageService.GetNotifications(userID, 100, 0)
	if err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage(err.Error()), nil)
		return
	}

	var notificationList []message_model.SystemNotification
	for _, n := range notifications {
		var fromUser *common.User
		if n.FromUser.ID != 0 {
			fromUser = &common.User{
				ID:            int64(n.FromUser.ID),
				Name:          n.FromUser.Name,
				FollowCount:   n.FromUser.FollowCount,
				FollowerCount: n.FromUser.FollowerCount,
				IsFollow:      false, // Logic for IsFollow would need relationship check
				Avatar:        n.FromUser.Avatar,
			}
		}

		notificationList = append(notificationList, message_model.SystemNotification{
			ID:         int64(n.ID),
			FromUser:   fromUser,
			Type:       string(n.Type),
			Content:    n.Content,
			CreateTime: n.CreatedAt.UnixMilli(),
			IsRead:     n.IsRead,
		})
	}

	if notificationList == nil {
		notificationList = []message_model.SystemNotification{}
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"notification_list": notificationList,
	})
}
