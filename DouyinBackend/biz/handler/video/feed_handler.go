package video

import (
	"context"
	"time"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	video_model "github.com/douyin/backend/biz/model/video"
)

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

	// Mock implementation
	mockAuthor := common.User{
		ID:            1,
		Name:          "MockAuthor",
		FollowCount:   100,
		FollowerCount: 1000,
		IsFollow:      false,
	}

	mockVideos := []common.Video{
		{
			ID:            1,
			Author:        mockAuthor,
			PlayURL:       "https://www.w3schools.com/html/mov_bbb.mp4",
			CoverURL:      "https://images.unsplash.com/photo-1611162617474-5b21e879e113",
			FavoriteCount: 120,
			CommentCount:  30,
			IsFavorite:    false,
			Title:         "Awesome Video 1",
		},
		{
			ID:            2,
			Author:        mockAuthor,
			PlayURL:       "https://www.w3schools.com/html/mov_bbb.mp4",
			CoverURL:      "https://images.unsplash.com/photo-1611162616475-46b635cb6868",
			FavoriteCount: 200,
			CommentCount:  50,
			IsFavorite:    true,
			Title:         "Awesome Video 2",
		},
	}

	c.JSON(consts.StatusOK, video_model.FeedResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		VideoList: mockVideos,
		NextTime:  time.Now().UnixMilli(),
	})
}
