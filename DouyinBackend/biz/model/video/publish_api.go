package video

import (
	"mime/multipart"

	"github.com/douyin/backend/biz/model/common"
)

type PublishActionRequest struct {
	Token string                `form:"token" vd:"$!='';msg:'token is required'"`
	Data  *multipart.FileHeader `form:"data" vd:"$!=nil;msg:'data is required'"`
	Title string                `form:"title" vd:"$!='';msg:'title is required'"`
}

type PublishActionResponse struct {
	common.BaseResponse
}

type PublishListRequest struct {
	UserID     int64  `query:"user_id" vd:"$>0;msg:'invalid user id'"`
	Token      string `query:"token"`
	LatestTime int64  `query:"latest_time"`
}

type PublishListResponse struct {
	common.BaseResponse
	NextTime  int64          `json:"next_time"`
	VideoList []common.Video `json:"video_list"`
}
