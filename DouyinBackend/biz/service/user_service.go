package service

import (
	"errors"
	"golang.org/x/crypto/bcrypt"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type UserService struct{}

func NewUserService() *UserService {
	return &UserService{}
}

func (s *UserService) Register(username, password string) (*model.User, error) {
	// Check if user exists
	var existingUser model.User
	if err := db.DB.Where("username = ?", username).First(&existingUser).Error; err == nil {
		return nil, errors.New("user already exists")
	}

	// Hash password
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return nil, err
	}

	// Create user
	user := model.User{
		Username: username,
		Password: string(hashedPassword),
		Name:     "User_" + username, // Default name
	}

	if err := db.DB.Create(&user).Error; err != nil {
		return nil, err
	}

	return &user, nil
}

func (s *UserService) Login(username, password string) (*model.User, error) {
	var user model.User
	if err := db.DB.Where("username = ?", username).First(&user).Error; err != nil {
		return nil, errors.New("user not found")
	}

	// Verify password
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password)); err != nil {
		return nil, errors.New("incorrect password")
	}

	return &user, nil
}

func (s *UserService) GetUserByID(userID uint) (*model.User, error) {
	var user model.User
	if err := db.DB.First(&user, userID).Error; err != nil {
		return nil, errors.New("user not found")
	}
	return &user, nil
}

func (s *UserService) UpdateProfile(userID uint, updates map[string]interface{}) error {
	if len(updates) == 0 {
		return nil
	}
	return db.DB.Model(&model.User{}).Where("id = ?", userID).Updates(updates).Error
}
