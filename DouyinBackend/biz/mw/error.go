package mw

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
)

// GlobalErrorHandler is a middleware that handles panic and returns a standardized response
func GlobalErrorHandler() app.HandlerFunc {
	return func(ctx context.Context, c *app.RequestContext) {
		defer func() {
			if err := recover(); err != nil {
				// In a real application, you would log the error and stack trace here
				// We can use ConvertErr to handle different types of panic values if needed
				utils.SendResponse(c, errno.ServiceErr.WithMessage("Internal Server Error"), nil)
				c.Abort()
			}
		}()
		c.Next(ctx)
	}
}
