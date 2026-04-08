package com.app.douyin.pro.feature.record.domain.model

data class FilterEffect(
    val name: String,
    val isDynamic: Boolean = false,
    val glslSource: String? = null
)
