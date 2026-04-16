package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.model.VideoPage
import com.app.douyin.pro.lib.media.model.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LoadMoreVideosUseCaseTest {

    @Mock
    private lateinit var mockRepository: HomeRepository

    private lateinit var loadMoreVideosUseCase: LoadMoreVideosUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        loadMoreVideosUseCase = LoadMoreVideosUseCase(mockRepository)
    }

    @Test
    fun `invoke should return more videos from repository`() = runBlocking {
        val nextTime = 123L
        val mockVideos = listOf(
            VideoModel(3L, "video3", "", "title3", 3L, "author3", null, 0L, 0L, 0L, false, false)
        )
        val mockPage = VideoPage(mockVideos, 456L)
        `when`(mockRepository.loadMoreVideos(nextTime)).thenReturn(Resource.Success(mockPage))

        val result = loadMoreVideosUseCase(nextTime)

        assert(result is Resource.Success)
        assertEquals(mockPage, (result as Resource.Success).data)
    }
}
