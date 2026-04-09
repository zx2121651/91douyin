package model

import (
	"gorm.io/gorm"
)

type Comment struct {
	gorm.Model
	UserID     uint      `gorm:"index;not null"`
	User       User      `gorm:"foreignKey:UserID"`
	VideoID    uint      `gorm:"index;not null"`
	Video      Video     `gorm:"foreignKey:VideoID"`
	Content    string    `gorm:"type:text;not null"`

	// Tree structure for nested replies
	ParentID   *uint     `gorm:"index"` // Nullable for top-level comments
	Parent     *Comment  `gorm:"foreignKey:ParentID"`
	Replies    []Comment `gorm:"foreignKey:ParentID"`
	ReplyCount int64     `gorm:"default:0"` // Tracks number of direct replies
}
