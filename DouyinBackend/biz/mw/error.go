package mw

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/model/common"
)

// GlobalErrorHandler is a middleware that handles panic and returns a standardized response
func GlobalErrorHandler() app.HandlerFunc {
	return func(ctx context.Context, c *app.RequestContext) {
		defer func() {
			if err := recover(); err != nil {
				// In a real application, you would log the error and stack trace here
				c.JSON(consts.StatusInternalServerError, common.BaseResponse{
					StatusCode: 500,
					StatusMsg:  "Internal Server Error",
				})
				c.Abort()
			}
		}()
		c.Next(ctx)
	}
}
