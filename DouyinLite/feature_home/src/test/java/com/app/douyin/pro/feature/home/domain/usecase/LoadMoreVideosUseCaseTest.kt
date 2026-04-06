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
        val page = 2
        val mockVideos = listOf(
            VideoModel(3, "video3", "", 3, "", null, "0", "0", "0", false, false)
        )
        `when`(mockRepository.loadMoreVideos(page)).thenReturn(Resource.Success(mockVideos))

        val result = loadMoreVideosUseCase(page)

        assert(result is Resource.Success)
        assertEquals(mockVideos, (result as Resource.Success).data)
    }
}
