package video

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
	video_model "github.com/douyin/backend/biz/model/video"
)

func ViewAction(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	var req video_model.ViewRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, video_model.ViewResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	err := videoService.RecordVideoView(uint(req.VideoID), currentUserID)
	if err != nil {
		c.JSON(consts.StatusInternalServerError, video_model.ViewResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	c.JSON(consts.StatusOK, video_model.ViewResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
	})
}
