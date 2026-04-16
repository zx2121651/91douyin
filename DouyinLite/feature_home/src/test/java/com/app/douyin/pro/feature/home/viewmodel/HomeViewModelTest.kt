package com.app.douyin.pro.feature.home.viewmodel

import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.usecase.GetVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.LoadMoreVideosUseCase
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.network.DouyinApiService
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val getVideosUseCase: GetVideosUseCase = mock(GetVideosUseCase::class.java)
    private val loadMoreVideosUseCase: LoadMoreVideosUseCase = mock(LoadMoreVideosUseCase::class.java)
    private val apiService: DouyinApiService = mock(DouyinApiService::class.java)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadInitialData success updates state with videos`() = runTest {
        val videos = listOf(VideoModel(1, "title", "author", "authorAvatar", 101, "playUrl", "coverUrl", "100", "50", "10", false, false))
        `when`(getVideosUseCase()).thenReturn(Resource.Success(videos))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.loadState is LoadState.Success)
        assertEquals(videos, state.videos)
        assertEquals(false, (state.loadState as LoadState.Success).isEmpty)
    }

    @Test
    fun `loadInitialData failure updates state with error`() = runTest {
        val errorMessage = "Network Error"
        `when`(getVideosUseCase()).thenReturn(Resource.Error(errorMessage))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.loadState is LoadState.Error)
        assertEquals(errorMessage, (state.loadState as LoadState.Error).message)
    }

    @Test
    fun `loadMore success appends videos to state`() = runTest {
        val initialVideos = listOf(VideoModel(1, "v1", "a1", "av1", 101, "p1", "c1", "1", "1", "1", false, false))
        val moreVideos = listOf(VideoModel(2, "v2", "a2", "av2", 102, "p2", "c2", "2", "2", "2", false, false))
        `when`(getVideosUseCase()).thenReturn(Resource.Success(initialVideos))
        `when`(loadMoreVideosUseCase(1)).thenReturn(Resource.Success(moreVideos))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.videos.size)
        assertEquals(initialVideos + moreVideos, state.videos)
        assertTrue(state.pagingState is PagingState.Idle)
    }

    @Test
    fun `loadMore failure updates pagingState with error`() = runTest {
        val initialVideos = listOf(VideoModel(1, "v1", "a1", "av1", 101, "p1", "c1", "1", "1", "1", false, false))
        val errorMessage = "Paging Error"
        `when`(getVideosUseCase()).thenReturn(Resource.Success(initialVideos))
        `when`(loadMoreVideosUseCase(1)).thenReturn(Resource.Error(errorMessage))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.videos.size)
        assertTrue(state.pagingState is PagingState.Error)
        assertEquals(errorMessage, (state.pagingState as PagingState.Error).message)
    }
}
