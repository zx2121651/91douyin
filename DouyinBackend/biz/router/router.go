package router

import (
	"github.com/cloudwego/hertz/pkg/app/server"
	"github.com/douyin/backend/biz/handler/user"
	"github.com/douyin/backend/biz/handler/video"
)

func Register(h *server.Hertz) {
	api := h.Group("/douyin")

	// User
	userGroup := api.Group("/user")
	userGroup.POST("/register/", user.Register)
	userGroup.POST("/login/", user.Login)
	userGroup.GET("/", user.Info)

	// Feed
	api.GET("/feed/", video.Feed)
}
