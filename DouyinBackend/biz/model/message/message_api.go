package message

import "github.com/douyin/backend/biz/model/common"

type Message struct {
	ID         int64  `json:"id"`
	ToUserID   int64  `json:"to_user_id"`
	FromUserID int64  `json:"from_user_id"`
	Content    string `json:"content"`
	CreateTime int64  `json:"create_time"`
}

type MessageActionRequest struct {
	Token      string `query:"token"`
	ToUserID   int64  `query:"to_user_id" vd:"$>0;msg:'invalid to_user_id'"`
	ActionType int32  `query:"action_type" vd:"$==1;msg:'action_type must be 1 (send message)'"`
	Content    string `query:"content" vd:"$!='';msg:'content is required'"`
}

type MessageActionResponse struct {
	common.BaseResponse
}

type MessageChatRequest struct {
	Token    string `query:"token"`
	ToUserID int64  `query:"to_user_id" vd:"$>0;msg:'invalid to_user_id'"`
	PreMsgTime int64 `query:"pre_msg_time"` // Last message time (optional)
}

type MessageChatResponse struct {
	common.BaseResponse
	MessageList []Message `json:"message_list"`
}
