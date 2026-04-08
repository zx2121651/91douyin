package com.app.douyin.pro.feature.record.domain.usecase

import com.app.douyin.pro.feature.record.data.RecordRepository
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetAvailableFiltersUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    operator fun invoke(): Resource<List<com.app.douyin.pro.feature.record.domain.model.FilterEffect>> = repository.getAvailableFilters()
}
