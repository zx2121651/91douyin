package video

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"time"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/config"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
	"github.com/douyin/backend/biz/model/common"
	video_model "github.com/douyin/backend/biz/model/video"
	"github.com/douyin/backend/biz/service"
	"github.com/douyin/backend/biz/service/storage"
)

var storageService = storage.NewStorageService()
var favoriteService = service.NewFavoriteService()
var relationService = service.NewRelationService()

func PublishAction(ctx context.Context, c *app.RequestContext) {
	var req video_model.PublishActionRequest
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

	filename := fmt.Sprintf("%d_%d_%s", userID, time.Now().Unix(), filepath.Base(req.Data.Filename))
	savePath := filepath.Join(config.GlobalConfig.Storage.Local.VideoPath, filename)

	if err := os.MkdirAll(filepath.Dir(savePath), 0755); err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage("Failed to create directory: "+err.Error()), nil)
		return
	}

	if err := c.SaveUploadedFile(req.Data, savePath); err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage("Failed to save video: "+err.Error()), nil)
		return
	}

	playURL := storageService.BuildVideoURL(filename)

	coverFilename := fmt.Sprintf("%d_%d_cover.jpg", userID, time.Now().Unix())

	// 占位封面图，提升发布接口响应速度
	placeholderCoverURL := "https://images.unsplash.com/photo-1611162617474-5b21e879e113"

	// 先将视频及占位封面存入数据库
	video, err := videoService.PublishVideo(userID, req.Title, playURL, placeholderCoverURL)
	if err != nil {
		storageService.DeleteFile(savePath)
		utils.SendResponse(c, errno.ServiceErr.WithMessage("Failed to publish video: "+err.Error()), nil)
		return
	}

	// 开启 goroutine 异步截取真实封面并更新数据库
	go func(vID uint, vPath, cName, pURL string) {
		actualCoverURL, err := storageService.GenerateCover(vPath, cName)
		if err != nil {
			// 异步处理失败，执行回滚
			storageService.DeleteFile(vPath)
			videoService.DeleteVideo(vID)
			return
		}

		if err := videoService.UpdateVideoStatusAndCover(vID, "published", actualCoverURL); err != nil {
			// 数据库更新失败，执行回滚
			storageService.DeleteFile(vPath)
			// 尝试删除已生成的封面文件
			coverPath := filepath.Join(config.GlobalConfig.Storage.Local.CoverPath, cName)
			storageService.DeleteFile(coverPath)
			videoService.DeleteVideo(vID)
			return
		}
	}(video.ID, savePath, coverFilename, playURL)

	utils.SendResponse(c, errno.Success, nil)
}

func PublishList(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}
	var req video_model.PublishListRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	limit := 30
	includeProcessing := currentUserID == uint(req.UserID)

	videos, nextTime, err := videoService.GetPublishList(uint(req.UserID), req.LatestTime, limit, includeProcessing)
	if err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage("Failed to get publish list: "+err.Error()), nil)
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
			Status:        v.Status,
			CreatedAt:     v.CreatedAt.UnixMilli(),
		})
	}

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"next_time":  nextTime,
		"video_list": commonVideos,
	})
}
