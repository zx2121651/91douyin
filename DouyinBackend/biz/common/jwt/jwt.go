package jwt

import (
	"errors"
	"time"
	"fmt"

	"github.com/golang-jwt/jwt/v4"
)

var SecretKey = []byte("douyin_secret_key_change_me_in_prod")

type Claims struct {
	UserID   uint   `json:"user_id"`
	Username string `json:"username"`
	jwt.RegisteredClaims
}

// GenerateToken generates a real JWT token for user
func GenerateToken(userID uint, username string) (string, error) {
	claims := Claims{
		UserID:   userID,
		Username: username,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(24 * 7 * time.Hour)), // 7 days expiration
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(SecretKey)
}

// ParseToken parses and validates the real JWT token
func ParseToken(tokenStr string) (uint, error) {
	if tokenStr == "" {
		return 0, errors.New("token is empty")
	}

	token, err := jwt.ParseWithClaims(tokenStr, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		// Verify the signing method is HMAC to prevent algorithm confusion attacks
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return SecretKey, nil
	})

	if err != nil {
		return 0, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims.UserID, nil
	}

	return 0, errors.New("invalid token")
}
