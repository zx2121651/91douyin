package service

import (
	"errors"
	"regexp"
	"strings"

	"golang.org/x/crypto/bcrypt"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type UserService struct{}

func NewUserService() *UserService {
	return &UserService{}
}

// Register performs strong input validation and secure hashing to create a new user
func (s *UserService) Register(username, password string) (*model.User, error) {
	username = strings.TrimSpace(username)
	password = strings.TrimSpace(password)

	// Validate Username Length (3-32 chars)
	if len(username) < 3 || len(username) > 32 {
		return nil, errors.New("username must be between 3 and 32 characters")
	}

	// Validate Username Characters (Alphanumeric and underscores)
	match, _ := regexp.MatchString("^[a-zA-Z0-9_]+$", username)
	if !match {
		return nil, errors.New("username can only contain letters, numbers, and underscores")
	}

	// Validate Password Length (6-32 chars)
	if len(password) < 6 || len(password) > 32 {
		return nil, errors.New("password must be between 6 and 32 characters")
	}

	// Check if user exists
	var existingUser model.User
	if err := db.DB.Where("username = ?", username).First(&existingUser).Error; err == nil {
		return nil, errors.New("user already exists")
	}

	// Hash password securely using bcrypt
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return nil, errors.New("failed to secure password")
	}

	// Create user with default profile details
	user := model.User{
		Username:      username,
		Password:      string(hashedPassword),
		Name:          "User_" + username,
		FollowCount:   0,
		FollowerCount: 0,
		Signature:     "欢迎来到真实的短视频世界", // Default signature
	}

	if err := db.DB.Create(&user).Error; err != nil {
		return nil, err
	}

	return &user, nil
}

// Login validates user credentials against the hashed password
func (s *UserService) Login(username, password string) (*model.User, error) {
	username = strings.TrimSpace(username)
	password = strings.TrimSpace(password)

	var user model.User
	// Ensure index-based lookup
	if err := db.DB.Where("username = ?", username).First(&user).Error; err != nil {
		return nil, errors.New("user not found or credentials invalid")
	}

	// Verify the hash against the submitted password
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(password)); err != nil {
		// Do not leak exact reason ("incorrect password") for security to prevent enumeration
		return nil, errors.New("user not found or credentials invalid")
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

	// Add business validation for update fields if necessary
	if name, ok := updates["name"].(string); ok {
		name = strings.TrimSpace(name)
		if len(name) == 0 || len(name) > 32 {
			return errors.New("invalid display name length")
		}
		updates["name"] = name
	}

	if signature, ok := updates["signature"].(string); ok {
		if len(signature) > 255 {
			return errors.New("signature is too long")
		}
	}

	return db.DB.Model(&model.User{}).Where("id = ?", userID).Updates(updates).Error
}
