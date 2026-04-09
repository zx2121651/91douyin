package comment

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	comment_model "github.com/douyin/backend/biz/model/comment"
	"github.com/douyin/backend/biz/model/common"
	"github.com/douyin/backend/biz/service"
)

var commentService = service.NewCommentService()
var relationService = service.NewRelationService()

func CommentAction(ctx context.Context, c *app.RequestContext) {
	var req comment_model.CommentActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, comment_model.CommentActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		c.JSON(consts.StatusUnauthorized, comment_model.CommentActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  "Unauthorized",
			},
		})
		return
	}
	userID := userIDRaw.(uint)

	if req.ActionType == 1 {
		if req.CommentText == "" {
			c.JSON(consts.StatusBadRequest, comment_model.CommentActionResponse{
				BaseResponse: common.BaseResponse{
					StatusCode: 1,
					StatusMsg:  "Comment text is required",
				},
			})
			return
		}

		var parentIDPtr *uint
		if req.ParentID != nil && *req.ParentID > 0 {
			val := uint(*req.ParentID)
			parentIDPtr = &val
		}
		comment, err := commentService.PostComment(userID, uint(req.VideoID), req.CommentText, parentIDPtr)
		if err != nil {
			c.JSON(consts.StatusInternalServerError, comment_model.CommentActionResponse{
				BaseResponse: common.BaseResponse{
					StatusCode: 1,
					StatusMsg:  err.Error(),
				},
			})
			return
		}

		c.JSON(consts.StatusOK, comment_model.CommentActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 0,
				StatusMsg:  "Success",
			},
			Comment: &comment_model.Comment{
				ID: int64(comment.ID),
				User: common.User{
					ID:            int64(comment.User.ID),
					Name:          comment.User.Name,
					FollowCount:   comment.User.FollowCount,
					FollowerCount: comment.User.FollowerCount,
					IsFollow:      relationService.IsFollow(userID, comment.User.ID),
					Avatar:        comment.User.Avatar,
					BackgroundImage: comment.User.BackgroundImage,
					Signature:     comment.User.Signature,
				},
				Content:    comment.Content,
				CreateDate: comment.CreatedAt.Format("01-02"),
				ReplyCount: comment.ReplyCount,
			},
		})

	} else if req.ActionType == 2 {
		if req.CommentID <= 0 {
			c.JSON(consts.StatusBadRequest, comment_model.CommentActionResponse{
				BaseResponse: common.BaseResponse{
					StatusCode: 1,
					StatusMsg:  "Comment ID is required for deletion",
				},
			})
			return
		}

		err := commentService.DeleteComment(userID, uint(req.CommentID), uint(req.VideoID))
		if err != nil {
			c.JSON(consts.StatusInternalServerError, comment_model.CommentActionResponse{
				BaseResponse: common.BaseResponse{
					StatusCode: 1,
					StatusMsg:  err.Error(),
				},
			})
			return
		}

		c.JSON(consts.StatusOK, comment_model.CommentActionResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 0,
				StatusMsg:  "Success",
			},
		})
	}
}

func CommentList(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}
	var req comment_model.CommentListRequest
	if err := c.BindAndValidate(&req); err != nil {
		c.JSON(consts.StatusBadRequest, comment_model.CommentListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	comments, err := commentService.GetCommentList(uint(req.VideoID))
	if err != nil {
		c.JSON(consts.StatusInternalServerError, comment_model.CommentListResponse{
			BaseResponse: common.BaseResponse{
				StatusCode: 1,
				StatusMsg:  err.Error(),
			},
		})
		return
	}

	var commonComments []comment_model.Comment
	for _, v := range comments {
		var replies []comment_model.Comment
		for _, reply := range v.Replies {
			replies = append(replies, comment_model.Comment{
				ID: int64(reply.ID),
				User: common.User{
					ID:            int64(reply.User.ID),
					Name:          reply.User.Name,
					FollowCount:   reply.User.FollowCount,
					FollowerCount: reply.User.FollowerCount,
					IsFollow:      relationService.IsFollow(currentUserID, reply.User.ID),
					Avatar:        reply.User.Avatar,
					BackgroundImage: reply.User.BackgroundImage,
					Signature:     reply.User.Signature,
				},
				Content:    reply.Content,
				CreateDate: reply.CreatedAt.Format("01-02"),
			})
		}

		commonComments = append(commonComments, comment_model.Comment{
			ID: int64(v.ID),
			User: common.User{
				ID:            int64(v.User.ID),
				Name:          v.User.Name,
				FollowCount:   v.User.FollowCount,
				FollowerCount: v.User.FollowerCount,
				IsFollow:      relationService.IsFollow(currentUserID, v.User.ID),
				Avatar:        v.User.Avatar,
				BackgroundImage: v.User.BackgroundImage,
				Signature:     v.User.Signature,
			},
			Content:    v.Content,
			CreateDate: v.CreatedAt.Format("01-02"),
			ReplyCount: v.ReplyCount,
			Replies:    replies,
		})
	}

	c.JSON(consts.StatusOK, comment_model.CommentListResponse{
		BaseResponse: common.BaseResponse{
			StatusCode: 0,
			StatusMsg:  "Success",
		},
		CommentList: commonComments,
	})
}
