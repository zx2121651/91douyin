package video

import "github.com/douyin/backend/biz/model/common"

type FeedRequest struct {
	LatestTime int64  `query:"latest_time"`
	Token      string `query:"token"`
}

type FeedResponse struct {
	common.BaseResponse
	VideoList []common.Video `json:"video_list"`
	NextTime  int64          `json:"next_time"`
}
