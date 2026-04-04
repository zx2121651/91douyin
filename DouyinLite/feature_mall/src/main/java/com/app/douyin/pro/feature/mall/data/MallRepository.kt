package com.app.douyin.pro.feature.mall.data

import com.app.douyin.pro.feature.mall.data.source.MallDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MallRepository @Inject constructor(
    private val dataSource: MallDataSource
) {
    fun getMallProducts() = dataSource.getProducts()
}
