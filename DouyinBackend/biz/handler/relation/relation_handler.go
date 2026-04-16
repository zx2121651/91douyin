package relation

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/douyin/backend/biz/model/common"
	relation_model "github.com/douyin/backend/biz/model/relation"
	"github.com/douyin/backend/biz/service"
)

var relationService = service.NewRelationService()

func RelationAction(ctx context.Context, c *app.RequestContext) {
	var req relation_model.RelationActionRequest
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

	err := relationService.RelationAction(userID, uint(req.ToUserID), req.ActionType)
	if err != nil {
		utils.SendResponse(c, err, nil)
		return
	}

	utils.SendResponse(c, errno.Success, nil)
}

func FollowList(ctx context.Context, c *app.RequestContext) {
	handleListRequest(c, relationService.GetFollowList)
}

func FollowerList(ctx context.Context, c *app.RequestContext) {
	handleListRequest(c, relationService.GetFollowerList)
}

func FriendList(ctx context.Context, c *app.RequestContext) {
	handleListRequest(c, relationService.GetFriendList)
}

func handleListRequest(c *app.RequestContext, getListFunc func(uint) ([]model.User, error)) {
	var req relation_model.RelationListRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	users, err := getListFunc(uint(req.UserID))
	if err != nil {
		utils.SendResponse(c, err, nil)
		return
	}

	var commonUsers []common.User
	for _, u := range users {
		commonUsers = append(commonUsers, common.User{
			ID:            int64(u.ID),
			Name:          u.Name,
			FollowCount:   u.FollowCount,
			FollowerCount: u.FollowerCount,
			IsFollow:      relationService.IsFollow(currentUserID, u.ID),
			Avatar:        u.Avatar,
			BackgroundImage: u.BackgroundImage,
			Signature:     u.Signature,
		})
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"user_list": commonUsers,
	})
}
