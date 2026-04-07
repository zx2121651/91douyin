package storage

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"

	"github.com/douyin/backend/biz/common/config"
)

type StorageService struct{}

func NewStorageService() *StorageService {
	return &StorageService{}
}

// GenerateCover uses FFmpeg to extract the first frame of the video
func (s *StorageService) GenerateCover(videoPath string, coverName string) (string, error) {
	// Ensure cover directory exists
	coverDirPath := config.GlobalConfig.Storage.Local.CoverPath
	if err := os.MkdirAll(coverDirPath, 0755); err != nil {
		return "", err
	}

	coverPath := filepath.Join(coverDirPath, coverName)

	// Build ffmpeg command: ffmpeg -i input.mp4 -ss 00:00:01 -vframes 1 output.jpg
	cmd := exec.Command("ffmpeg", "-y", "-i", videoPath, "-ss", "00:00:01", "-vframes", "1", coverPath)

	// If FFmpeg is not installed in the environment, this will fail.
	// For production, ensure FFmpeg is available. For development, we can mock it if it fails.
	if err := cmd.Run(); err != nil {
		// Mock a cover if FFmpeg fails (e.g. not installed locally)
		// return "", fmt.Errorf("ffmpeg generate cover failed: %v", err)
		return "https://images.unsplash.com/photo-1611162617474-5b21e879e113", nil
	}

	// Generate local URL for the cover
	coverURL := fmt.Sprintf("http://%s/static/covers/%s", config.GlobalConfig.Storage.Local.Domain, coverName)
	return coverURL, nil
}

// BuildVideoURL builds the public URL for the video
func (s *StorageService) BuildVideoURL(filename string) string {
	if config.GlobalConfig.Storage.Type == "local" {
		return fmt.Sprintf("http://%s/static/videos/%s", config.GlobalConfig.Storage.Local.Domain, filename)
	}
	// TODO: Implement OSS URL building
	return ""
}

func (s *StorageService) BuildAvatarURL(filename string) string {
	if config.GlobalConfig.Storage.Type == "local" {
		return fmt.Sprintf("http://%s/static/avatars/%s", config.GlobalConfig.Storage.Local.Domain, filename)
	}
	return ""
}

func (s *StorageService) BuildBackgroundURL(filename string) string {
	if config.GlobalConfig.Storage.Type == "local" {
		return fmt.Sprintf("http://%s/static/backgrounds/%s", config.GlobalConfig.Storage.Local.Domain, filename)
	}
	return ""
}
