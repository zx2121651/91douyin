package main

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/app/server"
	"github.com/cloudwego/hertz/pkg/common/utils"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/mw"
	"github.com/douyin/backend/biz/router"
)

func main() {
	// Initialize Database
	db.Init()

	h := server.Default(server.WithHostPorts("0.0.0.0:8080"), server.WithMaxRequestBodySize(100*1024*1024)) // 100MB max request size for video upload

	// Use global middleware
	h.Use(mw.GlobalErrorHandler())

	// Serve static files for uploaded videos
	h.Static("/static", "./public")

	h.GET("/ping", func(c context.Context, ctx *app.RequestContext) {
		ctx.JSON(consts.StatusOK, utils.H{"message": "pong"})
	})

	router.Register(h)

	h.Spin()
}
