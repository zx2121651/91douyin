package relation

import "github.com/douyin/backend/biz/model/common"

type RelationActionRequest struct {
	UserID     int64  `query:"user_id"`
	Token      string `query:"token"`
	ToUserID   int64  `query:"to_user_id" vd:"$>0;msg:'invalid to_user_id'"`
	ActionType int32  `query:"action_type" vd:"$==1||$==2;msg:'action_type must be 1 (follow) or 2 (unfollow)'"`
}

type RelationActionResponse struct {
	common.BaseResponse
}

type RelationListRequest struct {
	UserID int64  `query:"user_id" vd:"$>0;msg:'invalid user id'"`
	Token  string `query:"token"`
}

type RelationListResponse struct {
	common.BaseResponse
	UserList []common.User `json:"user_list"`
}
