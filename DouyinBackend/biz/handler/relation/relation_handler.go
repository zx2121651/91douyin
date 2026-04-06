package relation

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/douyin/backend/biz/model/common"
	relation_model "github.com/douyin/backend/biz/model/relation"
	"github.com/douyin/backend/biz/service"
)

var relationService = service.NewRelationService()

func RelationAction(ctx context.Context, c *app.RequestContext) {
	var req relation_model.RelationActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, relation_model.RelationActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, relation_model.RelationActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	userID := userIDRaw.(uint)

	err := relationService.RelationAction(userID, uint(req.ToUserID), req.ActionType)
	if err != nil {
		c.JSON(consts.StatusOK, relation_model.RelationActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	c.JSON(consts.StatusOK, relation_model.RelationActionResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
	})
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
		c.JSON(consts.StatusBadRequest, relation_model.RelationListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	users, err := getListFunc(uint(req.UserID))
	if err != nil {
		c.JSON(consts.StatusInternalServerError, relation_model.RelationListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
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
		})
	}

	c.JSON(consts.StatusOK, relation_model.RelationListResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		UserList: commonUsers,
	})
}
