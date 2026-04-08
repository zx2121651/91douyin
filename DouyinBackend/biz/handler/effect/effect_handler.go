package effect

import (
	"context"


	"github.com/cloudwego/hertz/pkg/app"
	"github.com/cloudwego/hertz/pkg/protocol/consts"

)

type EffectResponse struct {
	StatusCode int      `json:"status_code"`
	StatusMsg  string   `json:"status_msg,omitempty"`
	EffectList []Effect `json:"effect_list"`
}

type Effect struct {
	ID         int64  `json:"id"`
	Name       string `json:"name"`
	IsDynamic  bool   `json:"is_dynamic"`
	GlslSource string `json:"glsl_source,omitempty"`
}

func EffectList(ctx context.Context, c *app.RequestContext) {
	effects := []Effect{
		{
			ID:        1,
			Name:      "原片",
			IsDynamic: false,
		},
		{
			ID:        2,
			Name:      "黑白",
			IsDynamic: false,
		},
		{
			ID:        3,
			Name:      "RGB色散",
			IsDynamic: false,
		},
		{
			ID:        4,
			Name:      "二分屏",
			IsDynamic: false,
		},
		{
			ID:   5,
			Name: "动态波浪 (云端)",
			IsDynamic: true,
			GlslSource: `#version 310 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

in vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

out vec4 fragColor;

void main() {
    vec2 uv = vTextureCoord;
    // Add a dynamic wave effect based on the y coordinate
    uv.x += sin(uv.y * 10.0) * 0.05;

    fragColor = texture(sTexture, uv);
}`,
		},
        {
			ID:   6,
			Name: "黑客帝国 (云端)",
			IsDynamic: true,
			GlslSource: `#version 310 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

in vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

out vec4 fragColor;

void main() {
    vec4 color = texture(sTexture, vTextureCoord);
    // Green tint for matrix style
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    // Add some random static glitch
    float glitch = step(0.95, fract(vTextureCoord.y * 100.0 + color.g * 50.0)) * 0.2;

    fragColor = vec4(0.0, gray + glitch, 0.0, color.a);
}`,
		},
	}

	c.JSON(consts.StatusOK, EffectResponse{
		StatusCode: 0,
		StatusMsg:  "Success",
		EffectList: effects,
	})
}
