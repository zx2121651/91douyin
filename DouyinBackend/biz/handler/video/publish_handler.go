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
)

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

	// In a real application, you'd upload this to OSS/S3 and generate a thumbnail
	// Here we just save it locally for mock purpose
	filename := fmt.Sprintf("%d_%d_%s", userID, time.Now().Unix(), filepath.Base(req.Data.Filename))
	savePath := filepath.Join("public/videos", filename)

	if err := c.SaveUploadedFile(req.Data, savePath); err != nil {
		c.JSON(consts.StatusInternalServerError, video_model.PublishActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to save video: " + err.Error(),
			},
		})
		return
	}

	// Mock URLs
	serverAddr := string(c.URI().Host())
	if serverAddr == "" {
		serverAddr = "127.0.0.1:8080"
	}

	playURL := fmt.Sprintf("http://%s/static/videos/%s", serverAddr, filename)
	coverURL := "https://images.unsplash.com/photo-1611162617474-5b21e879e113" // Mock cover

	if err := videoService.PublishVideo(userID, req.Title, playURL, coverURL); err != nil {
		os.Remove(savePath) // Cleanup
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
				IsFollow:      false,
			},
			PlayURL:       v.PlayURL,
			CoverURL:      v.CoverURL,
			FavoriteCount: v.FavoriteCount,
			CommentCount:  v.CommentCount,
			IsFavorite:    false,
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
