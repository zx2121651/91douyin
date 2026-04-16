package utils

import (
	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"
	"github.com/douyin/backend/biz/common/errno"
)

// SendResponse pack response
func SendResponse(c *app.RequestContext, err error, data map[string]interface{}) {
	Err := errno.ConvertErr(err)

    // Douyin Lite App expects status_code and status_msg at the top level
    // Some responses might have additional fields.
    // We will merge data into the response if possible, or use a map.

    resp := make(map[string]interface{})
    resp["status_code"] = Err.StatusCode
    resp["status_msg"] = Err.StatusMsg

    for k, v := range data {
        resp[k] = v
    }

	c.JSON(consts.StatusOK, resp)
}
