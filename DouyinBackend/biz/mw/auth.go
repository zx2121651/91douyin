package mw

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/common/jwt"
	"github.com/douyin/backend/biz/model/common"
)

// AuthMiddleware is a middleware that verifies JWT token
func AuthMiddleware() app.HandlerFunc {
	return func(ctx context.Context, c *app.RequestContext) {
		// Token could be in query, form, or header depending on Douyin API specs
		token := c.Query("token")
		if token == "" {
			token = string(c.FormValue("token"))
		}

		if token == "" {
			c.JSON(consts.StatusUnauthorized, common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized: Token missing",
			})
			c.Abort()
			return
		}

		userID, err := jwt.ParseToken(token)
		if err != nil {
			c.JSON(consts.StatusUnauthorized, common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized: " + err.Error(),
			})
			c.Abort()
			return
		}

		// Set user ID to context for subsequent handlers
		c.Set("user_id", userID)

		c.Next(ctx)
	}
}

// SoftAuthMiddleware tries to parse token but doesn't abort if it's missing or invalid
// Useful for endpoints like Feed where token is optional
func SoftAuthMiddleware() app.HandlerFunc {
	return func(ctx context.Context, c *app.RequestContext) {
		token := c.Query("token")
		if token == "" {
			token = string(c.FormValue("token"))
		}

		if token != "" {
			userID, err := jwt.ParseToken(token)
			if err == nil {
				c.Set("user_id", userID)
			}
		}

		c.Next(ctx)
	}
}
