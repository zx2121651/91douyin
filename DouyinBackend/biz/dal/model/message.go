package model

import (
	"gorm.io/gorm"
)

type Message struct {
	gorm.Model
	ToUserID   uint   `gorm:"index;not null"`     // 接收者 ID
	FromUserID uint   `gorm:"index;not null"`     // 发送者 ID
	Content    string `gorm:"type:text;not null"` // 消息内容
	IsRead     bool   `gorm:"default:false"`      // 是否已读
}
