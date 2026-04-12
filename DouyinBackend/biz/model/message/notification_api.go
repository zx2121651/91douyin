package message

import "github.com/douyin/backend/biz/model/common"

type SystemNotification struct {
	ID         int64       `json:"id"`
	FromUser   *common.User `json:"from_user,omitempty"`
	Type       string      `json:"type"`
	Content    string      `json:"content"`
	CreateTime int64       `json:"create_time"`
	IsRead     bool        `json:"is_read"`
}

type NotificationListResponse struct {
	common.BaseResponse
	NotificationList []SystemNotification `json:"notification_list"`
}

type UnreadCountResponse struct {
	common.BaseResponse
	UnreadCount int64 `json:"unread_count"`
}
