package search

import "github.com/douyin/backend/biz/model/common"

type SearchVideoRequest struct {
	Token   string `query:"token"`
	Keyword string `query:"keyword" vd:"$!='';msg:'keyword is required'"`
	Cursor  int64  `query:"cursor"` // Used for pagination/offset
}

type SearchVideoResponse struct {
	common.BaseResponse
	VideoList []common.Video `json:"video_list"`
	NextCursor int64         `json:"next_cursor"`
	HasMore    bool          `json:"has_more"`
}

type SearchUserRequest struct {
	Token   string `query:"token"`
	Keyword string `query:"keyword" vd:"$!='';msg:'keyword is required'"`
	Cursor  int64  `query:"cursor"`
}

type SearchUserResponse struct {
	common.BaseResponse
	UserList []common.User `json:"user_list"`
	NextCursor int64       `json:"next_cursor"`
	HasMore    bool        `json:"has_more"`
}

type HotWordsResponse struct {
	common.BaseResponse
	Words []string `json:"words"`
}

type SuggestRequest struct {
	Keyword string `query:"keyword" vd:"$!='';msg:'keyword is required'"`
}

type SuggestResponse struct {
	common.BaseResponse
	Suggestions []string `json:"suggestions"`
}
