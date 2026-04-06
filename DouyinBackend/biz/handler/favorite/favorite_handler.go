package favorite

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	favorite_model "github.com/douyin/backend/biz/model/favorite"
	"github.com/douyin/backend/biz/service"
)

var favoriteService = service.NewFavoriteService()
var relationService = service.NewRelationService()

func FavoriteAction(ctx context.Context, c *app.RequestContext) {
	var req favorite_model.FavoriteActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, favorite_model.FavoriteActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, favorite_model.FavoriteActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	userID := userIDRaw.(uint)

	err := favoriteService.FavoriteAction(userID, uint(req.VideoID), req.ActionType)
	if err != nil {
		c.JSON(consts.StatusOK, favorite_model.FavoriteActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	c.JSON(consts.StatusOK, favorite_model.FavoriteActionResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
	})
}

func FavoriteList(ctx context.Context, c *app.RequestContext) {
	var req favorite_model.FavoriteListRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, favorite_model.FavoriteListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	targetUserID := uint(req.UserID)

	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	videos, err := favoriteService.GetFavoriteList(targetUserID)
	if err != nil {
		c.JSON(consts.StatusInternalServerError, favorite_model.FavoriteListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
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

	c.JSON(consts.StatusOK, favorite_model.FavoriteListResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		VideoList: commonVideos,
	})
}
