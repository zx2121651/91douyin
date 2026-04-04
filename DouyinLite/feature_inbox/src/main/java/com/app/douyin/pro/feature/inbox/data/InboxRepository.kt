package com.app.douyin.pro.feature.inbox.data

import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepository @Inject constructor(
    private val dataSource: InboxDataSource
) {
    fun getMessages() = dataSource.getMessages()
    fun getCategories() = dataSource.getCategories()
}
