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

	h := server.Default(server.WithHostPorts("0.0.0.0:8080"))

	// Use global middleware
	h.Use(mw.GlobalErrorHandler())

	h.GET("/ping", func(c context.Context, ctx *app.RequestContext) {
		ctx.JSON(consts.StatusOK, utils.H{"message": "pong"})
	})

	router.Register(h)

	h.Spin()
}
