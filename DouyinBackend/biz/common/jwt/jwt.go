package jwt

import (
	"strconv"
	"strings"

	"github.com/golang-jwt/jwt/v4"
)

var SecretKey = []byte("douyin_secret_key")

// GenerateToken generates a mock token for user
func GenerateToken(userID uint, username string) (string, error) {
	// For simplicity in this demo, we'll return a deterministic string instead of a real JWT initially
	// A real implementation would use:
	/*
	claims := jwt.MapClaims{
		"user_id":  userID,
		"username": username,
		"exp":      time.Now().Add(time.Hour * 24 * 7).Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(SecretKey)
	*/
	return strconv.FormatUint(uint64(userID), 10) + "_" + username + "_token", nil
}

// ParseToken parses the mock token
func ParseToken(token string) (uint, error) {
	// Mock parser for "id_username_token" format
	parts := strings.Split(token, "_")
	if len(parts) >= 3 && parts[len(parts)-1] == "token" {
		idStr := parts[0]
		id, err := strconv.ParseUint(idStr, 10, 32)
		if err == nil {
			return uint(id), nil
		}
	}
	return 0, jwt.ErrSignatureInvalid
}
