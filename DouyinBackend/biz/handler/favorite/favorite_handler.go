package favorite

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/douyin/backend/biz/model/common"
	favorite_model "github.com/douyin/backend/biz/model/favorite"
	"github.com/douyin/backend/biz/service"
)

var favoriteService = service.NewFavoriteService()
var relationService = service.NewRelationService()

func FavoriteAction(ctx context.Context, c *app.RequestContext) {
	var req favorite_model.FavoriteActionRequest
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

	err := favoriteService.FavoriteAction(userID, uint(req.VideoID), req.ActionType)
	if err != nil {
		utils.SendResponse(c, err, nil)
		return
	}

	utils.SendResponse(c, errno.Success, nil)
}

func FavoriteList(ctx context.Context, c *app.RequestContext) {
	var req favorite_model.FavoriteListRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	targetUserID := uint(req.UserID)

	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	// Check visibility rules
	if targetUserID != currentUserID {
		var targetUser model.User
		if err := db.DB.First(&targetUser, targetUserID).Error; err == nil {
			if !targetUser.FavoritePublic {
				utils.SendResponse(c, errno.Success, map[string]interface{}{
					"video_list": []common.Video{},
				})
				return
			}
		} else {
			utils.SendResponse(c, errno.UserNotFoundErr, nil)
			return
		}
	}

	videos, err := favoriteService.GetFavoriteList(targetUserID)
	if err != nil {
		utils.SendResponse(c, err, nil)
		return
	}

	var commonVideos []common.Video
	for _, v := range videos {
		commonVideos = append(commonVideos, common.Video{
			ID: int64(v.ID),
			Author: common.User{
				ID:            int64(v.Author.ID),
				Name:          v.Author.Name,
				FollowCount:   v.Author.FollowCount,
				FollowerCount: v.Author.FollowerCount,
				IsFollow:      relationService.IsFollow(currentUserID, v.Author.ID),
			},
			PlayURL:       v.PlayURL,
			CoverURL:      v.CoverURL,
			FavoriteCount: v.FavoriteCount,
			CommentCount:  v.CommentCount,
			IsFavorite:    favoriteService.IsFavorite(currentUserID, v.ID),
			Title:         v.Title,
		})
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"video_list": commonVideos,
	})
}
