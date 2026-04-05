package com.app.douyin.pro.feature.record.data

import com.app.douyin.pro.feature.record.data.source.RecordDataSource
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val dataSource: RecordDataSource
) {
    fun getAvailableFilters(): Resource<List<String>> = try {
        Resource.Success(dataSource.getFilters())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.UnknownError)
    }
}
