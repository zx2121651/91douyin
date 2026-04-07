package user

import (
	"mime/multipart"

	"github.com/douyin/backend/biz/model/common"
)

type UserRegisterRequest struct {
	Username string `query:"username" form:"username" vd:"$!='';msg:'username is required'"`
	Password string `query:"password" form:"password" vd:"$!='';msg:'password is required'"`
}

type UserRegisterResponse struct {
	common.BaseResponse
	UserID int64  `json:"user_id"`
	Token  string `json:"token"`
}

type UserLoginRequest struct {
	Username string `query:"username" form:"username" vd:"$!='';msg:'username is required'"`
	Password string `query:"password" form:"password" vd:"$!='';msg:'password is required'"`
}

type UserLoginResponse struct {
	common.BaseResponse
	UserID int64  `json:"user_id"`
	Token  string `json:"token"`
}

type UserInfoRequest struct {
	UserID int64  `query:"user_id" vd:"$>0;msg:'invalid user id'"`
	Token  string `query:"token"`
}

type UserInfoResponse struct {
	common.BaseResponse
	User common.User `json:"user"`
}

type UserProfileUpdateRequest struct {
	Token          string                `form:"token"`
	Name           string                `form:"name"`
	Signature      string                `form:"signature"`
	AvatarData     *multipart.FileHeader `form:"avatar"`
	BackgroundData *multipart.FileHeader `form:"background"`
}

type UserProfileUpdateResponse struct {
	common.BaseResponse
}
