package com.app.douyin.pro.feature.mall.data

import com.app.douyin.pro.feature.mall.data.source.MallDataSource
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MallRepository @Inject constructor(
    private val dataSource: MallDataSource
) {
    fun getMallProducts(): Resource<List<com.app.douyin.pro.feature.mall.domain.model.Product>> = Resource.Success(dataSource.getProducts())
}
