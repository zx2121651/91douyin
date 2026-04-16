package message

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
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
