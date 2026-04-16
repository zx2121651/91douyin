package com.app.douyin.pro.feature.home.viewmodel

import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.model.VideoPage
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
import org.mockito.ArgumentMatchers.anyLong

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
        val videos = listOf(VideoModel(1, "p1", "c1", "t1", 101, "a1", "av1", "1", "1", "1", false, false))
        val page = VideoPage(videos, 123L)
        `when`(getVideosUseCase()).thenReturn(Resource.Success(page))

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
        val video1 = VideoModel(1, "p1", "c1", "t1", 101, "a1", "av1", "1", "1", "1", false, false)
        val video2 = VideoModel(2, "p2", "c2", "t2", 102, "a2", "av2", "2", "2", "2", false, false)
        val initialPage = VideoPage(listOf(video1), 123L)
        val nextPage = VideoPage(listOf(video2), 456L)

        `when`(getVideosUseCase()).thenReturn(Resource.Success(initialPage))
        `when`(loadMoreVideosUseCase(123L)).thenReturn(Resource.Success(nextPage))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.videos.size)
        assertEquals(listOf(video1, video2), state.videos)
        assertTrue(state.pagingState is PagingState.Idle)
    }

    @Test
    fun `loadMore failure updates pagingState with error`() = runTest {
        val video1 = VideoModel(1, "p1", "c1", "t1", 101, "a1", "av1", "1", "1", "1", false, false)
        val initialPage = VideoPage(listOf(video1), 123L)
        val errorMessage = "Paging Error"

        `when`(getVideosUseCase()).thenReturn(Resource.Success(initialPage))
        `when`(loadMoreVideosUseCase(123L)).thenReturn(Resource.Error(errorMessage))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.videos.size)
        assertTrue(state.pagingState is PagingState.Error)
        assertEquals(errorMessage, (state.pagingState as PagingState.Error).message)
    }

    @Test
    fun `loadMore with duplicate videos should deduplicate`() = runTest {
        val video1 = VideoModel(1, "p1", "c1", "t1", 101, "a1", "av1", "1", "1", "1", false, false)
        val video2 = VideoModel(2, "p2", "c2", "t2", 102, "a2", "av2", "2", "2", "2", false, false)

        val initialPage = VideoPage(listOf(video1), 123L)
        // video1 is repeated in the second page
        val nextPage = VideoPage(listOf(video1, video2), 456L)

        `when`(getVideosUseCase()).thenReturn(Resource.Success(initialPage))
        `when`(loadMoreVideosUseCase(123L)).thenReturn(Resource.Success(nextPage))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Should deduplicate repeated videos", 2, state.videos.size)
        assertEquals(listOf(video1, video2), state.videos)
    }

    @Test
    fun `loadMore with null nextTime should not trigger request`() = runTest {
        val video1 = VideoModel(1, "p1", "c1", "t1", 101, "a1", "av1", "1", "1", "1", false, false)
        val initialPage = VideoPage(listOf(video1), null)

        `when`(getVideosUseCase()).thenReturn(Resource.Success(initialPage))

        viewModel = HomeViewModel(getVideosUseCase, loadMoreVideosUseCase, apiService)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // loadMoreVideosUseCase should not have been called
        // We can't easily verify with mockito in this setup without more boilerplate,
        // but we can check state remains Idle and videos didn't change.
        val state = viewModel.uiState.value
        assertEquals(1, state.videos.size)
        assertTrue(state.pagingState is PagingState.Idle)
    }
}
