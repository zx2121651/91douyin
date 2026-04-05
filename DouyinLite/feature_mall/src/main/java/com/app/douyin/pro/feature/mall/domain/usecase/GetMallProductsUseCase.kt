package com.app.douyin.pro.feature.mall.domain.usecase

import com.app.douyin.pro.feature.mall.data.MallRepository
import com.app.douyin.pro.feature.mall.domain.model.Product
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetMallProductsUseCase @Inject constructor(
    private val repository: MallRepository
) {
    operator fun invoke(): Resource<List<Product>> = repository.getMallProducts()
}
