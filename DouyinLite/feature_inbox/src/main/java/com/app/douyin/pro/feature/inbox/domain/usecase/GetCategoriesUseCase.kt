package com.app.douyin.pro.feature.inbox.domain.usecase

import com.app.douyin.pro.feature.inbox.data.InboxRepository
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: InboxRepository
) {
    operator fun invoke(): Resource<List<NotificationCategory>> = repository.getCategories()
}
