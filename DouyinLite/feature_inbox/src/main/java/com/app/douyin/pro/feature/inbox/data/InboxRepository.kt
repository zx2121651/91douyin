package com.app.douyin.pro.feature.inbox.data

import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepository @Inject constructor(
    private val dataSource: InboxDataSource
) {
    fun getMessages(): Resource<List<com.app.douyin.pro.feature.inbox.domain.model.Message>> = Resource.Success(dataSource.getMessages())
    fun getCategories(): Resource<List<com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory>> = Resource.Success(dataSource.getCategories())
}
