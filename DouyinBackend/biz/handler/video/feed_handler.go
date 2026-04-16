package video

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	video_model "github.com/douyin/backend/biz/model/video"
	"github.com/douyin/backend/biz/service"
)

var videoService = service.NewVideoService()
// favoriteService and relationService are defined in publish_handler.go within the same package

func Feed(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}
	var req video_model.FeedRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, video_model.FeedResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	videos, nextTime, err := videoService.GetFeed(req.LatestTime, 30, currentUserID)
	if err != nil {
		c.JSON(consts.StatusOK, video_model.FeedResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to get feed: " + err.Error(),
			},
		})
		return
	}

	// ---------------------------------------------------------
	// Advanced Architecture: Solve N+1 Query Problem for Feed List
	// ---------------------------------------------------------
	var videoIDs []uint
	var authorIDs []uint
	for _, v := range videos {
		videoIDs = append(videoIDs, v.ID)
		authorIDs = append(authorIDs, v.Author.ID)
	}

	// 1. Bulk query the favorite statuses
	favoriteMap, err := favoriteService.IsFavoriteMap(currentUserID, videoIDs)
	if err != nil {
		favoriteMap = make(map[uint]bool) // Fallback gracefully
	}

	// 2. Bulk query the follow statuses
	followMap, err := relationService.IsFollowMap(currentUserID, authorIDs)
	if err != nil {
		followMap = make(map[uint]bool) // Fallback gracefully
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
				IsFollow:      followMap[v.Author.ID],
			},
			PlayURL:       v.PlayURL,
			CoverURL:      v.CoverURL,
			FavoriteCount: v.FavoriteCount,
			CommentCount:  v.CommentCount,
			IsFavorite:    favoriteMap[v.ID],
			Title:         v.Title,
			Status:        v.Status,
			CreatedAt:     v.CreatedAt.UnixMilli(),
		})
	}

	c.JSON(consts.StatusOK, video_model.FeedResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		VideoList: commonVideos,
		NextTime:  nextTime,
	})
}
