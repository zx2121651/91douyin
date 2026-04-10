package model

import (
	"gorm.io/gorm"
)

// UserTag maps a user's affinity score to a specific content category
type UserTag struct {
	gorm.Model
	UserID        uint    `gorm:"uniqueIndex:idx_user_tag;not null"`
	CategoryTag   string  `gorm:"uniqueIndex:idx_user_tag;type:varchar(32);not null"`
	AffinityScore float64 `gorm:"default:0.0"`
}
