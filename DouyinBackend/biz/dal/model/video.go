package model

import (
	"gorm.io/gorm"
)

type Video struct {
	gorm.Model
	AuthorID      uint   `gorm:"index;not null"`
	Author        User   `gorm:"foreignKey:AuthorID"`
	PlayURL       string `gorm:"type:varchar(255);not null"`
	CoverURL      string `gorm:"type:varchar(255);not null"`
	FavoriteCount int64  `gorm:"default:0"`
	ViewCount     int64  `gorm:"default:0"`
	CommentCount  int64  `gorm:"default:0"`
	Title         string `gorm:"type:varchar(128);not null"`
	Status        string `gorm:"type:varchar(32);default:published"`
	CategoryTag   string `gorm:"type:varchar(32);default:"` // Used for recommendation engine
}
