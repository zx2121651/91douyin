file_path = "DouyinBackend/biz/service/video_service.go"
with open(file_path, "r") as f:
    content = f.read()

old_feed = """	// Recommendation System 2.0 (User-Item Collaborative Filtering concept)
	// If a user is logged in, we fetch their top affinity tags and boost matching videos
	var userTags []model.UserTag
	if currentUserID > 0 {
		db.DB.Where("user_id = ?", currentUserID).Order("affinity_score desc").Limit(3).Find(&userTags)
	}

	if len(userTags) > 0 {
		// Custom SQL to combine Hot Rank Decay with User Affinity Personalization
		// Score = Hotness + (UserTag Affinity * 10)
		tagStr1 := userTags[0].CategoryTag
		tagScore1 := userTags[0].AffinityScore

		query := `
			SELECT v.*,
			(
				((v.favorite_count * 2.0 + v.comment_count * 1.0) / POW((julianday('now') - julianday(v.created_at)) + 1.0, 1.5))
				+ CASE WHEN v.category_tag = ? THEN ? ELSE 0 END
			) as final_score
			FROM videos v
			WHERE v.status = 'published' AND v.deleted_at IS NULL
		`
		args := []interface{}{tagStr1, tagScore1 * 10.0}

		if latestTime > 0 {
			query += " AND v.created_at < ?"
			args = append(args, time.UnixMilli(latestTime))
		}

		query += " ORDER BY final_score DESC LIMIT ?"
		args = append(args, limit)

		if err := db.DB.Raw(query, args...).Scan(&videos).Error; err != nil {
			return nil, 0, err
		}

		// Preload authors manually for Raw scan
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

	} else {
		// Fallback to chronological feed if user has no data or is a guest
		query := db.DB.Preload("Author").Where("status = ?", "published").Order("created_at desc").Limit(limit)
		if latestTime > 0 {
			query = query.Where("created_at < ?", time.UnixMilli(latestTime))
		}
		if err := query.Find(&videos).Error; err != nil {
			return nil, 0, err
		}
	}"""

new_feed = """	// Recommendation System 3.0: Multi-Factor Hybrid Ranking with Exploration
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
	}"""

content = content.replace(old_feed, new_feed)

record_view_method = """
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
"""
content = content + record_view_method

with open(file_path, "w") as f:
    f.write(content)
