package com.app.douyin.pro.feature.inbox.domain.usecase

import com.app.douyin.pro.feature.inbox.data.InboxRepository
import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: InboxRepository
) {
    suspend operator fun invoke(): Resource<List<Message>> = repository.getMessages()
}
