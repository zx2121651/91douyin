package model

import (
	"gorm.io/gorm"
)

type Relation struct {
	gorm.Model
	UserID   uint `gorm:"uniqueIndex:idx_user_follow;not null"` // Follower
	FollowID uint `gorm:"uniqueIndex:idx_user_follow;not null"` // Followee
}
