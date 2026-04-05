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

func Feed(ctx context.Context, c *app.RequestContext) {
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

	// Default limit 30
	videos, nextTime, err := videoService.GetFeed(req.LatestTime, 30)
	if err != nil {
		c.JSON(consts.StatusOK, video_model.FeedResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Failed to get feed: " + err.Error(),
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

	c.JSON(consts.StatusOK, video_model.FeedResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		VideoList: commonVideos,
		NextTime:  nextTime,
	})
}
