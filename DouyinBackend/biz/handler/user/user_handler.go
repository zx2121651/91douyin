package user

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	user_model "github.com/douyin/backend/biz/model/user"
)

func Register(ctx context.Context, c *app.RequestContext) {
	var req user_model.UserRegisterRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, user_model.UserRegisterResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	// Mock implementation
	c.JSON(consts.StatusOK, user_model.UserRegisterResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		UserID: 1,
		Token:  "mock_token_" + req.Username,
	})
}

func Login(ctx context.Context, c *app.RequestContext) {
	var req user_model.UserLoginRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, user_model.UserLoginResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	// Mock implementation
	c.JSON(consts.StatusOK, user_model.UserLoginResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		UserID: 1,
		Token:  "mock_token_" + req.Username,
	})
}

func Info(ctx context.Context, c *app.RequestContext) {
	var req user_model.UserInfoRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, user_model.UserInfoResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	// Mock implementation
	c.JSON(consts.StatusOK, user_model.UserInfoResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		User: common.User{
			ID:            req.UserID,
			Name:          "TestUser",
			FollowCount:   10,
			FollowerCount: 20,
			IsFollow:      false,
		},
	})
}
