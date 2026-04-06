package video

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"time"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	video_model "github.com/douyin/backend/biz/model/video"
	"github.com/douyin/backend/biz/service"
)

var favoriteService = service.NewFavoriteService()
var relationService = service.NewRelationService()

func PublishAction(ctx context.Context, c *app.RequestContext) {
	var req video_model.PublishActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, video_model.PublishActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, video_model.PublishActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	userID := userIDRaw.(uint)

	filename := fmt.Sprintf("%d_%d_%s", userID, time.Now().Unix(), filepath.Base(req.Data.Filename))
	savePath := filepath.Join("public/videos", filename)

	if err := os.MkdirAll(filepath.Dir(savePath), 0755); err != nil {
		c.JSON(consts.StatusInternalServerError, video_model.PublishActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to create directory: " + err.Error(),
			},
		})
		return
	}

	if err := c.SaveUploadedFile(req.Data, savePath); err != nil {
		c.JSON(consts.StatusInternalServerError, video_model.PublishActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to save video: " + err.Error(),
			},
		})
		return
	}

	serverAddr := string(c.URI().Host())
	if serverAddr == "" {
		serverAddr = "127.0.0.1:8080"
	}

	playURL := fmt.Sprintf("http://%s/static/videos/%s", serverAddr, filename)
	coverURL := "https://images.unsplash.com/photo-1611162617474-5b21e879e113"

	if err := videoService.PublishVideo(userID, req.Title, playURL, coverURL); err != nil {
		os.Remove(savePath)
		c.JSON(consts.StatusInternalServerError, video_model.PublishActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to publish video: " + err.Error(),
			},
		})
		return
	}

	c.JSON(consts.StatusOK, video_model.PublishActionResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
	})
}

func PublishList(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}
	var req video_model.PublishListRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, video_model.PublishListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	videos, err := videoService.GetPublishList(uint(req.UserID))
	if err != nil {
		c.JSON(consts.StatusInternalServerError, video_model.PublishListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to get publish list: " + err.Error(),
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

	c.JSON(consts.StatusOK, video_model.PublishListResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		VideoList: commonVideos,
	})
}
