package router

import (
	"github.com/cloudwego/hertz/pkg/app/server"
	"github.com/douyin/backend/biz/handler/comment"
	"github.com/douyin/backend/biz/handler/favorite"
	"github.com/douyin/backend/biz/handler/relation"
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

	// Favorite
	favoriteGroup := api.Group("/favorite")
	favoriteGroup.POST("/action/", mw.AuthMiddleware(), favorite.FavoriteAction)
	favoriteGroup.GET("/list/", mw.SoftAuthMiddleware(), favorite.FavoriteList)

	// Comment
	commentGroup := api.Group("/comment")
	commentGroup.POST("/action/", mw.AuthMiddleware(), comment.CommentAction)
	commentGroup.GET("/list/", mw.SoftAuthMiddleware(), comment.CommentList)

	// Relation
	relationGroup := api.Group("/relation")
	relationGroup.POST("/action/", mw.AuthMiddleware(), relation.RelationAction)
	relationGroup.GET("/follow/list/", mw.SoftAuthMiddleware(), relation.FollowList)
	relationGroup.GET("/follower/list/", mw.SoftAuthMiddleware(), relation.FollowerList)
	relationGroup.GET("/friend/list/", mw.SoftAuthMiddleware(), relation.FriendList)
}
