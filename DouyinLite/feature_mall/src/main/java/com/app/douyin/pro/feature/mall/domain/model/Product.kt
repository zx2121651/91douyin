package com.app.douyin.pro.feature.mall.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: String,
    val imageUrl: String = ""
)
