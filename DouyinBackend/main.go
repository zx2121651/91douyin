package main

import (
	"context"
	"fmt"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/app/server"
	"github.com/cloudwego/hertz/pkg/common/utils"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/common/config"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/mw"
	"github.com/douyin/backend/biz/router"
)

func main() {
	// Initialize Config
	config.Init()

	// Initialize Database
	db.Init()

	addr := fmt.Sprintf("%s:%d", config.GlobalConfig.Server.Address, config.GlobalConfig.Server.Port)
	h := server.Default(server.WithHostPorts(addr), server.WithMaxRequestBodySize(config.GlobalConfig.Server.MaxRequestSize))

	// Use global middleware
	h.Use(mw.GlobalErrorHandler())

	// Serve static files for uploaded videos and generated covers
	h.Static("/static/videos", config.GlobalConfig.Storage.Local.VideoPath)
	h.Static("/static/covers", config.GlobalConfig.Storage.Local.CoverPath)
	h.Static("/static/avatars", config.GlobalConfig.Storage.Local.AvatarPath)
	h.Static("/static/backgrounds", config.GlobalConfig.Storage.Local.BackgroundPath)

	h.GET("/ping", func(c context.Context, ctx *app.RequestContext) {
		ctx.JSON(consts.StatusOK, utils.H{"message": "pong"})
	})

	router.Register(h)

	h.Spin()
}
