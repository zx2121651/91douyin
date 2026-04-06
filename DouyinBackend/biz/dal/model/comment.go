package model

import (
	"gorm.io/gorm"
)

type Comment struct {
	gorm.Model
	UserID  uint   `gorm:"index;not null"`
	User    User   `gorm:"foreignKey:UserID"`
	VideoID uint   `gorm:"index;not null"`
	Video   Video  `gorm:"foreignKey:VideoID"`
	Content string `gorm:"type:text;not null"`
}
