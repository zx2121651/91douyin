package video

import (
	"encoding/json"
	"testing"

	"github.com/cloudwego/hertz/pkg/app/server"
	"github.com/cloudwego/hertz/pkg/common/test/assert"
	"github.com/cloudwego/hertz/pkg/common/ut"
	"github.com/douyin/backend/biz/common/config"
	"github.com/douyin/backend/biz/dal/db"
	"github.com/douyin/backend/biz/dal/model"
)

func TestFeedHandler(t *testing.T) {
	// Setup DB
	config.GlobalConfig = &config.Config{
		Database: config.DatabaseConfig{
			DSN: ":memory:",
		},
	}
	db.Init()

	// Seed data
	user := model.User{Name: "handler_test_user"}
	db.DB.Create(&user)
	for i := 0; i < 5; i++ {
		db.DB.Create(&model.Video{
			AuthorID: user.ID,
			Title:    "Handler Video",
			Status:   "published",
		})
	}

	t.Run("Normal Feed Request", func(t *testing.T) {
		h := server.New()
		h.GET("/douyin/feed/", Feed)

		w := ut.PerformRequest(h.Engine, "GET", "/douyin/feed/", nil)
		resp := w.Result()
		assert.DeepEqual(t, 200, resp.StatusCode())

		var body map[string]interface{}
		err := json.Unmarshal(resp.Body(), &body)
		assert.DeepEqual(t, nil, err)
		assert.DeepEqual(t, float64(0), body["status_code"])

		videoList := body["video_list"].([]interface{})
		assert.True(t, len(videoList) > 0)
		assert.NotNil(t, body["next_time"])
	})

	t.Run("Invalid latest_time format", func(t *testing.T) {
		h := server.New()
		h.GET("/douyin/feed/", Feed)

		// Hertz BindAndValidate might fail if latest_time is not a number
		w := ut.PerformRequest(h.Engine, "GET", "/douyin/feed/?latest_time=abc", nil)
		resp := w.Result()
		// Should return error response instead of panic
		assert.DeepEqual(t, 200, resp.StatusCode())
		var body map[string]interface{}
		json.Unmarshal(resp.Body(), &body)
		assert.True(t, body["status_code"].(float64) != 0)
	})

	t.Run("Empty Feed", func(t *testing.T) {
		// Clean up videos
		db.DB.Exec("DELETE FROM videos")

		h := server.New()
		h.GET("/douyin/feed/", Feed)

		w := ut.PerformRequest(h.Engine, "GET", "/douyin/feed/", nil)
		resp := w.Result()
		assert.DeepEqual(t, 200, resp.StatusCode())

		var body map[string]interface{}
		json.Unmarshal(resp.Body(), &body)
		assert.DeepEqual(t, float64(0), body["status_code"])
		// Check for next_time. If it's missing or nil, that might be why it failed.
		assert.DeepEqual(t, float64(0), body["next_time"])

		if body["video_list"] != nil {
			videoList := body["video_list"].([]interface{})
			assert.DeepEqual(t, 0, len(videoList))
		}
	})
}
