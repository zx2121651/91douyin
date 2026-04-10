package com.app.douyin.pro.feature.inbox.data

import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepository @Inject constructor(
    private val dataSource: InboxDataSource
) {
    suspend fun getMessages(): Resource<List<com.app.douyin.pro.feature.inbox.domain.model.Message>> = try {
        Resource.Success(dataSource.getMessages())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }
    suspend fun getCategories(): Resource<List<com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory>> = Resource.Success(dataSource.getCategories())
}
