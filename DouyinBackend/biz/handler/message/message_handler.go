package message

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	message_model "github.com/douyin/backend/biz/model/message"
	"github.com/douyin/backend/biz/service"
)

var messageService = service.NewMessageService()

func MessageAction(ctx context.Context, c *app.RequestContext) {
	var req message_model.MessageActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, message_model.MessageActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, message_model.MessageActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	userID := userIDRaw.(uint)

	err := messageService.SendMessage(userID, uint(req.ToUserID), req.Content)
	if err != nil {
		c.JSON(consts.StatusInternalServerError, message_model.MessageActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	c.JSON(consts.StatusOK, message_model.MessageActionResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
	})
}

func MessageChat(ctx context.Context, c *app.RequestContext) {
	var req message_model.MessageChatRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, message_model.MessageChatResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, message_model.MessageChatResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	userID := userIDRaw.(uint)

	messages, err := messageService.GetChatHistory(userID, uint(req.ToUserID), req.PreMsgTime)
	if err != nil {
		c.JSON(consts.StatusInternalServerError, message_model.MessageChatResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
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

	c.JSON(consts.StatusOK, message_model.MessageChatResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		MessageList: messageList,
	})
}
