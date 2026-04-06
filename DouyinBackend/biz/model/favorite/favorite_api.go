package favorite

import "github.com/douyin/backend/biz/model/common"

type FavoriteActionRequest struct {
	UserID     int64  `query:"user_id"`
	Token      string `query:"token"`
	VideoID    int64  `query:"video_id" vd:"$>0;msg:'invalid video id'"`
	ActionType int32  `query:"action_type" vd:"$==1||$==2;msg:'action_type must be 1 (like) or 2 (unlike)'"`
}

type FavoriteActionResponse struct {
	common.BaseResponse
}

type FavoriteListRequest struct {
	UserID int64  `query:"user_id" vd:"$>0;msg:'invalid user id'"`
	Token  string `query:"token"`
}

type FavoriteListResponse struct {
	common.BaseResponse
	VideoList []common.Video `json:"video_list"`
}
