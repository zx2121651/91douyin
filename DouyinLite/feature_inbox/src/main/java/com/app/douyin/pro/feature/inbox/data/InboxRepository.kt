package com.app.douyin.pro.feature.inbox.data

import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource
import com.app.douyin.pro.feature.inbox.domain.model.Conversation
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.feature.inbox.domain.model.NotificationItem
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepository @Inject constructor(
    private val dataSource: InboxDataSource
) {
    suspend fun getMessages(): Resource<List<Conversation>> = try {
        Resource.Success(dataSource.getMessages())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }
    suspend fun getCategories(): Resource<List<NotificationCategory>> = Resource.Success(dataSource.getCategories())

    suspend fun getNotifications(): Resource<List<NotificationItem>> = try {
        Resource.Success(dataSource.getNotifications())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }
}
