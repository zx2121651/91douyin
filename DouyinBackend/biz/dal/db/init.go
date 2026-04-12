package db

import (
	"log"
	"os"

	"github.com/douyin/backend/biz/dal/model"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"github.com/douyin/backend/biz/common/config"

	"gorm.io/gorm/logger"
)

var DB *gorm.DB

func Init() {
	var err error
	// Use SQLite for local development
	dbPath := config.GlobalConfig.Database.DSN

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

	// 优化 SQLite 并发性能 (WAL 模式)
	DB.Exec("PRAGMA journal_mode=WAL;")
	DB.Exec("PRAGMA busy_timeout=5000;")
	DB.Exec("PRAGMA synchronous=NORMAL;")

	// Migrate the schema
	err = DB.AutoMigrate(&model.User{}, &model.Video{}, &model.Favorite{}, &model.Comment{}, &model.Relation{}, &model.Message{})
	if err != nil {
		panic("failed to migrate database")
	}
}
