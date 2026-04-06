package model

import (
	"gorm.io/gorm"
)

type User struct {
	gorm.Model
	Username      string  `gorm:"type:varchar(32);uniqueIndex;not null"`
	Password      string  `gorm:"type:varchar(255);not null"`
	Name          string  `gorm:"type:varchar(32);not null"`
	FollowCount   int64   `gorm:"default:0"`
	FollowerCount int64   `gorm:"default:0"`
	Avatar        string  `gorm:"type:varchar(255)"`
	Signature     string  `gorm:"type:varchar(255)"`
	Videos        []Video `gorm:"foreignKey:AuthorID"`
}
