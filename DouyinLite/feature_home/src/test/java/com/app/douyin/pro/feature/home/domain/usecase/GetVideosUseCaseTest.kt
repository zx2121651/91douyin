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

class GetVideosUseCaseTest {

    @Mock
    private lateinit var mockRepository: HomeRepository

    private lateinit var getVideosUseCase: GetVideosUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        getVideosUseCase = GetVideosUseCase(mockRepository)
    }

    @Test
    fun `invoke should return videos from repository`() = runBlocking {
        val mockVideos = listOf(
            VideoModel(1L, "video1", "", "title1", 1L, "author1", null, 0L, 0L, 0L, false, false),
            VideoModel(2L, "video2", "", "title2", 2L, "author2", null, 0L, 0L, 0L, false, false)
        )
        val mockPage = VideoPage(mockVideos, 123L)
        `when`(mockRepository.getInitialVideos()).thenReturn(Resource.Success(mockPage))

        val result = getVideosUseCase()

        assert(result is Resource.Success)
        assertEquals(mockPage, (result as Resource.Success).data)
    }
}
