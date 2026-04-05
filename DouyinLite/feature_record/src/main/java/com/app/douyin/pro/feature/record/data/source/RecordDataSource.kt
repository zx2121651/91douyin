package com.app.douyin.pro.feature.record.data.source

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordDataSource @Inject constructor() {
    fun getFilters() = listOf("原图", "胶片", "复古", "黑白", "唯美", "冷白")
}
