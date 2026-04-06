package search

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	search_model "github.com/douyin/backend/biz/model/search"
	"github.com/douyin/backend/biz/service"
)

var searchService = service.NewSearchService()
var favoriteService = service.NewFavoriteService()
var relationService = service.NewRelationService()

func SearchVideo(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	var req search_model.SearchVideoRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, search_model.SearchVideoResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	limit := 10 // Paging limit
	offset := int(req.Cursor)

	videos, err := searchService.SearchVideos(req.Keyword, offset, limit)
	if err != nil {
		c.JSON(consts.StatusInternalServerError, search_model.SearchVideoResponse{
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
				Avatar:        v.Author.Avatar,
				BackgroundImage: v.Author.BackgroundImage,
				Signature:     v.Author.Signature,
			},
			PlayURL:       v.PlayURL,
			CoverURL:      v.CoverURL,
			FavoriteCount: v.FavoriteCount,
			CommentCount:  v.CommentCount,
			IsFavorite:    favoriteService.IsFavorite(currentUserID, v.ID),
			Title:         v.Title,
		})
	}

	hasMore := len(videos) == limit
	var nextCursor int64 = 0
	if hasMore {
		nextCursor = req.Cursor + int64(limit)
	}

	c.JSON(consts.StatusOK, search_model.SearchVideoResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		VideoList:  commonVideos,
		NextCursor: nextCursor,
		HasMore:    hasMore,
	})
}

func SearchUser(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	var req search_model.SearchUserRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, search_model.SearchUserResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	limit := 10 // Paging limit
	offset := int(req.Cursor)

	users, err := searchService.SearchUsers(req.Keyword, offset, limit)
	if err != nil {
		c.JSON(consts.StatusInternalServerError, search_model.SearchUserResponse{
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
			Avatar:        u.Avatar,
			BackgroundImage: u.BackgroundImage,
			Signature:     u.Signature,
		})
	}

	hasMore := len(users) == limit
	var nextCursor int64 = 0
	if hasMore {
		nextCursor = req.Cursor + int64(limit)
	}

	c.JSON(consts.StatusOK, search_model.SearchUserResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		UserList:   commonUsers,
		NextCursor: nextCursor,
		HasMore:    hasMore,
	})
}
