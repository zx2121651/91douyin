package search

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
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
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	limit := 10 // Paging limit
	offset := int(req.Cursor)

	videos, err := searchService.SearchVideos(req.Keyword, offset, limit)
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

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"video_list":  commonVideos,
		"next_cursor": nextCursor,
		"has_more":    hasMore,
	})
}

func SearchUser(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	var req search_model.SearchUserRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	limit := 10 // Paging limit
	offset := int(req.Cursor)

	users, err := searchService.SearchUsers(req.Keyword, offset, limit)
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

	hasMore := len(users) == limit
	var nextCursor int64 = 0
	if hasMore {
		nextCursor = req.Cursor + int64(limit)
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"user_list":   commonUsers,
		"next_cursor": nextCursor,
		"has_more":    hasMore,
	})
}
