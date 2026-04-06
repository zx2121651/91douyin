package user

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/common/jwt"
	"github.com/douyin/backend/biz/model/common"
	user_model "github.com/douyin/backend/biz/model/user"
	"fmt"
	"os"
	"path/filepath"
	"time"
	"github.com/douyin/backend/biz/service"
	"github.com/douyin/backend/biz/service/storage"
	"github.com/douyin/backend/biz/common/config"
)

var storageService = storage.NewStorageService()


var userService = service.NewUserService()
var relationService = service.NewRelationService()

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

	uidRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, user_model.UserInfoResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	uid := uidRaw.(uint)

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
			IsFollow:      relationService.IsFollow(uid, user.ID),
			Avatar:        user.Avatar,
			BackgroundImage: user.BackgroundImage,
			Signature:     user.Signature,
		},
	})
}

func UpdateProfile(ctx context.Context, c *app.RequestContext) {
	var req user_model.UserProfileUpdateRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, user_model.UserProfileUpdateResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	uidRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, user_model.UserProfileUpdateResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	uid := uidRaw.(uint)

	updates := make(map[string]interface{})

	if req.Name != "" {
		updates["name"] = req.Name
	}
	if req.Signature != "" {
		updates["signature"] = req.Signature
	}

	// Handle Avatar Upload
	if req.AvatarData != nil {
		filename := fmt.Sprintf("%d_%d_avatar_%s", uid, time.Now().Unix(), filepath.Base(req.AvatarData.Filename))
		savePath := filepath.Join(config.GlobalConfig.Storage.Local.AvatarPath, filename)
		if err := os.MkdirAll(filepath.Dir(savePath), 0755); err != nil {
			// ignore or handle
		} else if err := c.SaveUploadedFile(req.AvatarData, savePath); err == nil {
			avatarURL := storageService.BuildAvatarURL(filename)
			updates["avatar"] = avatarURL
		}
	}

	// Handle Background Upload
	if req.BackgroundData != nil {
		filename := fmt.Sprintf("%d_%d_bg_%s", uid, time.Now().Unix(), filepath.Base(req.BackgroundData.Filename))
		savePath := filepath.Join(config.GlobalConfig.Storage.Local.BackgroundPath, filename)
		if err := os.MkdirAll(filepath.Dir(savePath), 0755); err != nil {
			// ignore
		} else if err := c.SaveUploadedFile(req.BackgroundData, savePath); err == nil {
			bgURL := storageService.BuildBackgroundURL(filename)
			updates["background_image"] = bgURL
		}
	}

	if len(updates) > 0 {
		if err := userService.UpdateProfile(uid, updates); err != nil {
			c.JSON(consts.StatusInternalServerError, user_model.UserProfileUpdateResponse{
				BaseResponse: common.BaseResponse{
					StatusCode: 1,
					StatusMsg:  "Failed to update profile: " + err.Error(),
				},
			})
			return
		}
	}

	c.JSON(consts.StatusOK, user_model.UserProfileUpdateResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
	})
}
