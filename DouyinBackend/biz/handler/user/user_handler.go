package user

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/common/jwt"
	"github.com/douyin/backend/biz/model/common"
	user_model "github.com/douyin/backend/biz/model/user"
	"github.com/douyin/backend/biz/service"
)

var userService = service.NewUserService()

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

	user, err := userService.Register(req.Username, req.Password)
	if err != nil {
		c.JSON(consts.StatusOK, user_model.UserRegisterResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	token, _ := jwt.GenerateToken(user.ID, user.Username)

	c.JSON(consts.StatusOK, user_model.UserRegisterResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		UserID: int64(user.ID),
		Token:  token,
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

	user, err := userService.Login(req.Username, req.Password)
	if err != nil {
		c.JSON(consts.StatusOK, user_model.UserLoginResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	token, _ := jwt.GenerateToken(user.ID, user.Username)

	c.JSON(consts.StatusOK, user_model.UserLoginResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		UserID: int64(user.ID),
		Token:  token,
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

	// Verify token (simplified)
	uid, err := jwt.ParseToken(req.Token)
	if err != nil || int64(uid) != req.UserID {
		c.JSON(consts.StatusUnauthorized, user_model.UserInfoResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Invalid token",
			},
		})
		return
	}

	user, err := userService.GetUserByID(uint(req.UserID))
	if err != nil {
		c.JSON(consts.StatusOK, user_model.UserInfoResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "User not found",
			},
		})
		return
	}

	c.JSON(consts.StatusOK, user_model.UserInfoResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		User: common.User{
			ID:            int64(user.ID),
			Name:          user.Name,
			FollowCount:   user.FollowCount,
			FollowerCount: user.FollowerCount,
			IsFollow:      false, // Needs Follow system implementation
		},
	})
}
