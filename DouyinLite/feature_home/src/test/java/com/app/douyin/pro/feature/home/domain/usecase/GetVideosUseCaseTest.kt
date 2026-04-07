package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.lib.media.model.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class GetVideosUseCaseTest {

    @Mock
    lateinit var repository: HomeRepository

    private lateinit var getVideosUseCase: GetVideosUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getVideosUseCase = GetVideosUseCase(repository)
    }

    @Test
    fun testInvokeReturnsVideosFromRepository() = runBlocking {
        val mockData = listOf(
            com.app.douyin.pro.lib.media.network.VideoDto(1, "video1", "", 0, 0, false, "title", com.app.douyin.pro.lib.media.network.UserDto(1, "author", "", false)),
            com.app.douyin.pro.lib.media.network.VideoDto(2, "video2", "", 0, 0, false, "title", com.app.douyin.pro.lib.media.network.UserDto(1, "author", "", false))
        )
        Mockito.`when`(repository.getInitialVideos()).thenReturn(Resource.Success(mockData))

        val result = getVideosUseCase()

        assert(result is Resource.Success)
        assertEquals(mockData, (result as Resource.Success).data)
    }
}
