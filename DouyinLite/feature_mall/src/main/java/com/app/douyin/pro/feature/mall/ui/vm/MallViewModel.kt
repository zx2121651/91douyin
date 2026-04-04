package com.app.douyin.pro.feature.mall.ui.vm

import androidx.lifecycle.ViewModel
import com.app.douyin.pro.feature.mall.data.MallRepository
import com.app.douyin.pro.feature.mall.domain.model.Product
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MallViewModel @Inject constructor(
    private val repository: MallRepository
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    init {
        val result = repository.getMallProducts()
        if (result is Resource.Success) {
            _products.value = result.data
        }
    }
}
