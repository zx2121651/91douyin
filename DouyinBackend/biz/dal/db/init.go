package db

import (
	"log"
	"os"

	"github.com/douyin/backend/biz/dal/model"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

func Init() {
	var err error
	// Use SQLite for local development
	dbPath := "douyin.db"

	newLogger := logger.New(
		log.New(os.Stdout, "\r\n", log.LstdFlags),
		logger.Config{
			LogLevel: logger.Info, // Log SQL queries
		},
	)

	DB, err = gorm.Open(sqlite.Open(dbPath), &gorm.Config{
		Logger: newLogger,
	})
	if err != nil {
		panic("failed to connect database")
	}

	// Migrate the schema
	err = DB.AutoMigrate(&model.User{}, &model.Video{})
	if err != nil {
		panic("failed to migrate database")
	}
}
