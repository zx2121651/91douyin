package model

import (
	"gorm.io/gorm"
)

type Favorite struct {
	gorm.Model
	UserID  uint `gorm:"uniqueIndex:idx_user_video;not null"`
	VideoID uint `gorm:"uniqueIndex:idx_user_video;not null"`
}
