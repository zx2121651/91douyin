package router

import (
	"github.com/cloudwego/hertz/pkg/app/server"
	"github.com/douyin/backend/biz/handler/user"
	"github.com/douyin/backend/biz/handler/video"
	"github.com/douyin/backend/biz/mw"
)

func Register(h *server.Hertz) {
	api := h.Group("/douyin")

	// User
	userGroup := api.Group("/user")
	userGroup.POST("/register/", user.Register)
	userGroup.POST("/login/", user.Login)
	userGroup.GET("/", mw.AuthMiddleware(), user.Info)

	// Feed
	api.GET("/feed/", mw.SoftAuthMiddleware(), video.Feed)

	// Publish
	publishGroup := api.Group("/publish")
	publishGroup.POST("/action/", mw.AuthMiddleware(), video.PublishAction)
	publishGroup.GET("/list/", mw.SoftAuthMiddleware(), video.PublishList)
}
