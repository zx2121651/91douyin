package service

import (
	"errors"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
	"gorm.io/gorm"
)

type RelationService struct{}

func NewRelationService() *RelationService {
	return &RelationService{}
}

func (s *RelationService) RelationAction(userID uint, toUserID uint, actionType int32) error {
	if userID == toUserID {
		return errors.New("cannot follow yourself")
	}

	return db.DB.Transaction(func(tx *gorm.DB) error {
		var toUser model.User
		if err := tx.First(&toUser, toUserID).Error; err != nil {
			return errors.New("target user not found")
		}

		var user model.User
		if err := tx.First(&user, userID).Error; err != nil {
			return errors.New("user not found")
		}

		relation := model.Relation{
			UserID:   userID,
			FollowID: toUserID,
		}

		if actionType == 1 { // Follow
			err := tx.Where("user_id = ? AND follow_id = ?", userID, toUserID).First(&model.Relation{}).Error
			if err == nil {
				return errors.New("already followed")
			} else if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}

			if err := tx.Create(&relation).Error; err != nil {
				return err
			}

			// Update counts
			if err := tx.Model(&user).Update("follow_count", gorm.Expr("follow_count + ?", 1)).Error; err != nil {
				return err
			}
			if err := tx.Model(&toUser).Update("follower_count", gorm.Expr("follower_count + ?", 1)).Error; err != nil {
				return err
			}

		} else if actionType == 2 { // Unfollow
			res := tx.Unscoped().Where("user_id = ? AND follow_id = ?", userID, toUserID).Delete(&model.Relation{})
			if res.Error != nil {
				return res.Error
			}
			if res.RowsAffected > 0 {
				if user.FollowCount > 0 {
					if err := tx.Model(&user).Update("follow_count", gorm.Expr("follow_count - ?", 1)).Error; err != nil {
						return err
					}
				}
				if toUser.FollowerCount > 0 {
					if err := tx.Model(&toUser).Update("follower_count", gorm.Expr("follower_count - ?", 1)).Error; err != nil {
						return err
					}
				}
			} else {
				return errors.New("not followed yet")
			}
		} else {
			return errors.New("invalid action type")
		}

		return nil
	})
}

func (s *RelationService) GetFollowList(userID uint) ([]model.User, error) {
	var relations []model.Relation
	if err := db.DB.Where("user_id = ?", userID).Find(&relations).Error; err != nil {
		return nil, err
	}

	if len(relations) == 0 {
		return []model.User{}, nil
	}

	var followIDs []uint
	for _, rel := range relations {
		followIDs = append(followIDs, rel.FollowID)
	}

	var users []model.User
	if err := db.DB.Where("id IN ?", followIDs).Find(&users).Error; err != nil {
		return nil, err
	}

	return users, nil
}

func (s *RelationService) GetFollowerList(userID uint) ([]model.User, error) {
	var relations []model.Relation
	if err := db.DB.Where("follow_id = ?", userID).Find(&relations).Error; err != nil {
		return nil, err
	}

	if len(relations) == 0 {
		return []model.User{}, nil
	}

	var followerIDs []uint
	for _, rel := range relations {
		followerIDs = append(followerIDs, rel.UserID)
	}

	var users []model.User
	if err := db.DB.Where("id IN ?", followerIDs).Find(&users).Error; err != nil {
		return nil, err
	}

	return users, nil
}

func (s *RelationService) GetFriendList(userID uint) ([]model.User, error) {
	// A friend is someone who follows you, and you follow back
	followers, err := s.GetFollowerList(userID)
	if err != nil {
		return nil, err
	}

	follows, err := s.GetFollowList(userID)
	if err != nil {
		return nil, err
	}

	followMap := make(map[uint]bool)
	for _, u := range follows {
		followMap[u.ID] = true
	}

	var friends []model.User
	for _, u := range followers {
		if followMap[u.ID] {
			friends = append(friends, u)
		}
	}

	return friends, nil
}

// IsFollow checks if userID follows toUserID
func (s *RelationService) IsFollow(userID uint, toUserID uint) bool {
	if userID == 0 || toUserID == 0 {
		return false
	}
	var count int64
	db.DB.Model(&model.Relation{}).Where("user_id = ? AND follow_id = ?", userID, toUserID).Count(&count)
	return count > 0
}


// IsFollowMap performs a bulk lookup to check if a user follows a set of users (solves N+1 query problem)
func (s *RelationService) IsFollowMap(userID uint, targetUserIDs []uint) (map[uint]bool, error) {
	resultMap := make(map[uint]bool)
	if userID == 0 || len(targetUserIDs) == 0 {
		for _, id := range targetUserIDs {
			resultMap[id] = false
		}
		return resultMap, nil
	}

	var relations []model.Relation
	if err := db.DB.Where("user_id = ? AND follow_id IN ?", userID, targetUserIDs).Find(&relations).Error; err != nil {
		return nil, err
	}

	for _, rel := range relations {
		resultMap[rel.FollowID] = true
	}

	for _, id := range targetUserIDs {
		if _, exists := resultMap[id]; !exists {
			resultMap[id] = false
		}
	}

	return resultMap, nil
}
