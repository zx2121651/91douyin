package service

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/douyin/backend/biz/common/config"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/service/storage"
	"github.com/douyin/backend/biz/dal/model"
	"github.com/stretchr/testify/assert"
)

func TestVideoRollback(t *testing.T) {
	// Initialize config and DB for test
	config.GlobalConfig = &config.Config{
		Database: config.DatabaseConfig{
			DSN: ":memory:",
		},
		Storage: config.StorageConfig{
			Local: config.LocalConfig{
				VideoPath: "./test_videos",
				CoverPath: "./test_covers",
			},
		},
	}
	db.Init()

	videoService := NewVideoService()
	storageService := storage.NewStorageService()

	t.Run("TestDeleteVideo", func(t *testing.T) {
		video, err := videoService.PublishVideo(1, "test title", "play_url", "cover_url")
		assert.NoError(t, err)
		assert.NotNil(t, video)

		err = videoService.DeleteVideo(video.ID)
		assert.NoError(t, err)

		var count int64
		db.DB.Model(&model.Video{}).Where("id = ?", video.ID).Count(&count)
		// It should be 1 if it's soft deleted, but let's check Unscoped
		db.DB.Unscoped().Model(&model.Video{}).Where("id = ?", video.ID).Count(&count)
		assert.Equal(t, int64(1), count)

		db.DB.Model(&model.Video{}).Where("id = ?", video.ID).Count(&count)
		assert.Equal(t, int64(0), count)
	})

	t.Run("TestDeleteFile", func(t *testing.T) {
		err := os.MkdirAll("./test_videos", 0755)
		assert.NoError(t, err)
		defer os.RemoveAll("./test_videos")

		filePath := filepath.Join("./test_videos", "test_file.txt")
		err = os.WriteFile(filePath, []byte("test content"), 0644)
		assert.NoError(t, err)

		err = storageService.DeleteFile(filePath)
		assert.NoError(t, err)

		_, err = os.Stat(filePath)
		assert.True(t, os.IsNotExist(err))
	})

	t.Run("TestUpdateVideoStatusAndCover", func(t *testing.T) {
		video, err := videoService.PublishVideo(1, "test title", "play_url", "placeholder")
		assert.NoError(t, err)

		err = videoService.UpdateVideoStatusAndCover(video.ID, "published", "actual_cover")
		assert.NoError(t, err)

		var updatedVideo model.Video
		db.DB.First(&updatedVideo, video.ID)
		assert.Equal(t, "published", updatedVideo.Status)
		assert.Equal(t, "actual_cover", updatedVideo.CoverURL)
	})
}
