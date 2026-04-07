package com.app.douyin.pro.feature.record.domain.usecase

import com.app.douyin.pro.feature.record.data.RecordRepository
import com.app.douyin.pro.lib.media.model.Resource
import java.io.File
import javax.inject.Inject

class PublishVideoUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(videoFile: File, title: String): Resource<Unit> {
        return repository.publishVideo(videoFile, title)
    }
}
