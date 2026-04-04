package com.app.douyin.pro.feature.record.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CountdownUseCase @Inject constructor() {
    operator fun invoke(seconds: Int): Flow<Int> = flow {
        for (i in seconds downTo 0) {
            emit(i)
            if (i > 0) delay(1000)
        }
    }
}
