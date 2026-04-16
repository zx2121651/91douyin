package comment

import (
	"context"

	"github.com/cloudwego/hertz/pkg/app"
	"github.com/douyin/backend/biz/common/errno"
	"github.com/douyin/backend/biz/common/utils"
	comment_model "github.com/douyin/backend/biz/model/comment"
	"github.com/douyin/backend/biz/model/common"
	"github.com/douyin/backend/biz/service"
)

var commentService = service.NewCommentService()
var relationService = service.NewRelationService()

func CommentAction(ctx context.Context, c *app.RequestContext) {
	var req comment_model.CommentActionRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	userIDRaw, exists := c.Get("user_id")
	if !exists {
		utils.SendResponse(c, errno.AuthErr, nil)
		return
	}
	userID := userIDRaw.(uint)

	if req.ActionType == 1 {
		if req.CommentText == "" {
			utils.SendResponse(c, errno.ParamErr.WithMessage("Comment text is required"), nil)
			return
		}

		var parentIDPtr *uint
		if req.ParentID != nil && *req.ParentID > 0 {
			val := uint(*req.ParentID)
			parentIDPtr = &val
		}
		comment, err := commentService.PostComment(userID, uint(req.VideoID), req.CommentText, parentIDPtr, req.IdempotencyKey)
		if err != nil {
			utils.SendResponse(c, err, nil)
			return
		}

		utils.SendResponse(c, errno.Success, map[string]interface{}{
			"comment": &comment_model.Comment{
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
			utils.SendResponse(c, errno.ParamErr.WithMessage("Comment ID is required for deletion"), nil)
			return
		}

		err := commentService.DeleteComment(userID, uint(req.CommentID), uint(req.VideoID))
		if err != nil {
			utils.SendResponse(c, err, nil)
			return
		}

		utils.SendResponse(c, errno.Success, nil)
	}
}

func CommentList(ctx context.Context, c *app.RequestContext) {
	var currentUserID uint = 0
	if rawID, exists := c.Get("user_id"); exists {
		currentUserID = rawID.(uint)
	}
	var req comment_model.CommentListRequest
	if err := c.BindAndValidate(&req); err != nil {
		utils.SendResponse(c, errno.ParamErr.WithMessage(err.Error()), nil)
		return
	}

	comments, err := commentService.GetCommentList(uint(req.VideoID))
	if err != nil {
		utils.SendResponse(c, err, nil)
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

	utils.SendResponse(c, errno.Success, map[string]interface{}{
		"comment_list": commonComments,
	})
}
