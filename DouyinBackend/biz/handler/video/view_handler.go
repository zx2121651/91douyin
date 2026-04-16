package video

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
	video_model "github.com/douyin/backend/biz/model/video"
)

func ViewAction(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}

	var req video_model.ViewRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	err := videoService.RecordVideoView(uint(req.VideoID), currentUserID)
	if err != nil {
		utils.SendResponse(c, errno.ServiceErr.WithMessage(err.Error()), nil)
		return
	}

	utils.SendResponse(c, errno.Success, nil)
}
