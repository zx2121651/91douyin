package service

import (
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

type SearchService struct{}

func NewSearchService() *SearchService {
	return &SearchService{}
}

// SearchVideos searches for videos using a weighted hot rank formula based on interaction and time decay
func (s *SearchService) SearchVideos(keyword string, offset int, limit int) ([]model.Video, error) {
	var videos []model.Video

	// Hot Rank Formula = (FavoriteCount * 2 + CommentCount * 1) / (Time_Since_Upload_In_Days + 1)^1.5
	// This ensures that fresh content isn't completely overshadowed by historically popular content,
	// while still elevating quality interactions.
	query := `
		SELECT videos.*,
		((videos.favorite_count * 2.0 + videos.comment_count * 1.0) / POW((julianday('now') - julianday(videos.created_at)) + 1.0, 1.5)) as hot_score
		FROM videos
		WHERE videos.deleted_at IS NULL AND videos.title LIKE ?
		ORDER BY hot_score DESC, videos.created_at DESC
		LIMIT ? OFFSET ?
	`

	if err := db.DB.Raw(query, "%"+keyword+"%", limit, offset).Scan(&videos).Error; err != nil {
		return nil, err
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

	return videos, nil
}

// SearchUsers searches for users containing the keyword in name or username
func (s *SearchService) SearchUsers(keyword string, offset int, limit int) ([]model.User, error) {
	var users []model.User

	if err := db.DB.Where("name LIKE ? OR username LIKE ?", "%"+keyword+"%", "%"+keyword+"%").
		Order("follower_count desc, created_at desc").
		Offset(offset).Limit(limit).Find(&users).Error; err != nil {
		return nil, err
	}

	return users, nil
}

// GetHotWords returns a list of trending search terms
func (s *SearchService) GetHotWords() ([]string, error) {
	// For now, we return a static list of hot words.
	// In a real scenario, this would come from a cache or a separate analytics service.
	return []string{
		"杭州亚运会",
		"国庆假期旅游攻略",
		"iPhone 15 发布",
		"程序员的一天",
		"猫咪卖萌瞬间",
		"硬核科技评测",
		"抖音电影榜",
		"秋天的第一杯奶茶",
	}, nil
}

// GetSuggestions returns a list of search suggestions based on the keyword
func (s *SearchService) GetSuggestions(keyword string) ([]string, error) {
	var suggestions []string

	// Suggestion logic: search for video titles starting with the keyword
	// or containing the keyword, limited to 10 results.
	query := `
		SELECT DISTINCT title
		FROM videos
		WHERE videos.deleted_at IS NULL AND videos.title LIKE ?
		LIMIT 10
	`
	if err := db.DB.Raw(query, keyword+"%").Scan(&suggestions).Error; err != nil {
		return nil, err
	}

	// If not enough suggestions from titles, we could also add from usernames or other sources
	if len(suggestions) < 10 {
		var userNames []string
		userQuery := `
			SELECT DISTINCT name
			FROM users
			WHERE users.deleted_at IS NULL AND (users.name LIKE ? OR users.username LIKE ?)
			LIMIT ?
		`
		if err := db.DB.Raw(userQuery, keyword+"%", keyword+"%", 10-len(suggestions)).Scan(&userNames).Error; err == nil {
			suggestions = append(suggestions, userNames...)
		}
	}

	return suggestions, nil
}
