package model

import (
	"gorm.io/gorm"
)

type NotificationType string

const (
	NotificationTypeLike    NotificationType = "like"
	NotificationTypeComment NotificationType = "comment"
	NotificationTypeFollow  NotificationType = "follow"
	NotificationTypeSystem  NotificationType = "system"
)

type SystemNotification struct {
	gorm.Model
	ToUserID   uint             `gorm:"index;not null"`
	FromUserID uint             `gorm:"index"` // Nullable for system generated
	FromUser   User             `gorm:"foreignKey:FromUserID"`
	Type       NotificationType `gorm:"type:varchar(32);not null"`
	Content    string           `gorm:"type:text"`
	TargetID   uint             // Could be VideoID or CommentID depending on type
	IsRead     bool             `gorm:"default:false"`
}
