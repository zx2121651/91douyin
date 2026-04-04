package com.app.douyin.pro.feature.mall.data

import com.app.douyin.pro.feature.mall.domain.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MallRepository @Inject constructor() {
    fun getMallProducts(): List<Product> {
        return listOf(
            Product("1", "运动耳机 Pro", "¥199"),
            Product("2", "智能手环", "¥299"),
            Product("3", "旅行背包", "¥159"),
            Product("4", "便携麦克风", "¥89"),
            Product("5", "直播补光灯", "¥129")
        )
    }
}
