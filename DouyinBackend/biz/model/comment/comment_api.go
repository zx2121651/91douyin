package comment

import "github.com/douyin/backend/biz/model/common"

type Comment struct {
	ID         int64       `json:"id"`
	User       common.User `json:"user"`
	Content    string      `json:"content"`
	CreateDate string      `json:"create_date"`
}

type CommentActionRequest struct {
	UserID      int64  `query:"user_id"`
	Token       string `query:"token"`
	VideoID     int64  `query:"video_id" vd:"$>0;msg:'invalid video id'"`
	ActionType  int32  `query:"action_type" vd:"$==1||$==2;msg:'action_type must be 1 (post) or 2 (delete)'"`
	CommentText string `query:"comment_text"`
	CommentID   int64  `query:"comment_id"`
}

type CommentActionResponse struct {
	common.BaseResponse
	Comment *Comment `json:"comment,omitempty"`
}

type CommentListRequest struct {
	Token   string `query:"token"`
	VideoID int64  `query:"video_id" vd:"$>0;msg:'invalid video id'"`
}

type CommentListResponse struct {
	common.BaseResponse
	CommentList []Comment `json:"comment_list"`
}
