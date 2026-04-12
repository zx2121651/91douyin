package service

import (
	"time"
	"gorm.io/gorm"

	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type VideoService struct{}

func NewVideoService() *VideoService {
	return &VideoService{}
}

func (s *VideoService) GetFeed(latestTime int64, limit int, currentUserID uint) ([]model.Video, int64, error) {
	var videos []model.Video

	// Recommendation System 3.0: Multi-Factor Hybrid Ranking with Exploration
	var userTags []model.UserTag
	if currentUserID > 0 {
		db.DB.Where("user_id = ?", currentUserID).Order("affinity_score desc").Limit(3).Find(&userTags)
	}

	// We build a robust query mapping up to 3 dominant user tags
	// Formula:
	// Global Base (0-100) = (Views*0.1 + Likes*2.0 + Comments*1.5) / TimeDecay
	// Personalization (0-100) = sum(Tag_Affinity_Score * weights if video.tag == user_tag)
	// Exploration (0-10) = Random Noise to break filter bubbles

	query := `
		SELECT v.*,
		(
			((v.view_count * 0.1 + v.favorite_count * 2.0 + v.comment_count * 1.5) / POW((julianday('now') - julianday(v.created_at)) + 1.0, 1.2))
	`
	args := []interface{}{}

	if len(userTags) > 0 {
		for i, ut := range userTags {
			// Diminishing returns on lesser tags
			weight := 1.0 / float64(i+1)
			query += " + (CASE WHEN v.category_tag = ? THEN ? ELSE 0 END) "
			args = append(args, ut.CategoryTag, ut.AffinityScore * weight * 10.0)
		}
	} else {
		// No personalization, just use the base score
		query += " + 0 "
	}

	// Add random noise factor (-5 to +5) for serendipity and exploration
	query += ` + (RANDOM() % 10 - 5.0) ) as final_score
		FROM videos v
		WHERE v.status = 'published' AND v.deleted_at IS NULL
	`

	if latestTime > 0 {
		query += " AND v.created_at < ?"
		args = append(args, time.UnixMilli(latestTime))
	}

	query += " ORDER BY final_score DESC LIMIT ?"
	args = append(args, limit)

	if err := db.DB.Raw(query, args...).Scan(&videos).Error; err != nil {
		return nil, 0, err
	}

	// Because Raw scan doesn't auto-preload relations in GORM, we manually load the authors
	var authorIDs []uint
	for _, v := range videos {
		authorIDs = append(authorIDs, v.AuthorID)
	}
	if len(authorIDs) > 0 {
		var authors []model.User
		if err := db.DB.Where("id IN ?", authorIDs).Find(&authors).Error; err == nil {
			authorMap := make(map[uint]model.User)
			for _, a := range authors {
				authorMap[a.ID] = a
			}
			for i, v := range videos {
				videos[i].Author = authorMap[v.AuthorID]
			}
		}
	}

	var nextTime int64
	if len(videos) > 0 {
		nextTime = videos[len(videos)-1].CreatedAt.UnixMilli()
	} else {
		nextTime = time.Now().UnixMilli()
	}

	return videos, nextTime, nil
}

func (s *VideoService) PublishVideo(authorID uint, title string, playURL string, coverURL string) error {
	// Step 1: Immediately persist the video as "processing" to ensure the user gets a fast response
	video := model.Video{
		AuthorID:      authorID,
		PlayURL:       playURL,
		CoverURL:      coverURL,
		Title:         title,
		FavoriteCount: 0,
		CommentCount:  0,
		Status:        "processing",
		CategoryTag:   "comedy", // In reality, an ML model would auto-classify this
	}

	if err := db.DB.Create(&video).Error; err != nil {
		return err
	}

	// Step 2: Spin off a Goroutine to handle asynchronous media post-processing and checks
	go func(vid uint, uid uint) {
		// Simulate network extraction, FFmpeg transcoding delay, and content safety moderation
		time.Sleep(3 * time.Second)

		// After processing, update the status to "published" safely inside a transaction
		db.DB.Transaction(func(tx *gorm.DB) error {
			if err := tx.Model(&model.Video{}).Where("id = ?", vid).Update("status", "published").Error; err != nil {
				return err
			}

			// Generate a system notification to the author confirming successful processing
			notif := model.SystemNotification{
				ToUserID:   uid,
				Type:       model.NotificationTypeSystem,
				Content:    "您的视频《" + title + "》已发布成功",
				TargetID:   vid,
			}
			tx.Create(&notif)

			return nil
		})
	}(video.ID, authorID)

	return nil
}

func (s *VideoService) GetPublishList(userID uint) ([]model.Video, error) {
	var videos []model.Video

	if err := db.DB.Preload("Author").Where("author_id = ? AND status = ?", userID, "published").Order("created_at desc").Find(&videos).Error; err != nil {
		return nil, err
	}

	return videos, nil
}

// RecordVideoView implicitly increments user affinity and global view counts,
// simulating the core mechanism of "Playtime" and "View" signals.
func (s *VideoService) RecordVideoView(videoID uint, userID uint) error {
	// Increment Global View Count securely
	if err := db.DB.Model(&model.Video{}).Where("id = ?", videoID).Update("view_count", gorm.Expr("view_count + 1")).Error; err != nil {
		return err
	}

	// If the user is logged in, implicitly boost their affinity for this video's category by a small fractional amount (0.1)
	if userID > 0 {
		var video model.Video
		if err := db.DB.Select("category_tag").Where("id = ?", videoID).First(&video).Error; err == nil && video.CategoryTag != "" {
			var ut model.UserTag
			if err := db.DB.Where("user_id = ? AND category_tag = ?", userID, video.CategoryTag).First(&ut).Error; err != nil {
				db.DB.Create(&model.UserTag{UserID: userID, CategoryTag: video.CategoryTag, AffinityScore: 0.1})
			} else {
				db.DB.Model(&ut).Update("affinity_score", gorm.Expr("affinity_score + ?", 0.1))
			}
		}
	}

	return nil
}
