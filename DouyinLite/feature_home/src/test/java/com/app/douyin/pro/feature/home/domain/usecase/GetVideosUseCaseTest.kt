package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.feature.home.domain.model.VideoModel
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
            VideoModel(1, "video1", "", 1, "", null, "0", "0", "0", false, false),
            VideoModel(2, "video2", "", 2, "", null, "0", "0", "0", false, false)
        )
        `when`(mockRepository.getInitialVideos()).thenReturn(Resource.Success(mockVideos))

        val result = getVideosUseCase()

        assert(result is Resource.Success)
        assertEquals(mockVideos, (result as Resource.Success).data)
    }
}
