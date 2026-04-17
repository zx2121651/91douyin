package com.app.douyin.pro.feature.home.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.app.douyin.pro.feature.home.data.SearchRepository
import com.app.douyin.pro.feature.home.domain.usecase.SearchUsersUseCase
import com.app.douyin.pro.feature.home.domain.usecase.SearchVideosUseCase
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.VideoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private val searchVideosUseCase: SearchVideosUseCase = mock(SearchVideosUseCase::class.java)
    private val searchUsersUseCase: SearchUsersUseCase = mock(SearchUsersUseCase::class.java)
    private val searchRepository: SearchRepository = mock(SearchRepository::class.java)
    private val interactionManager: VideoInteractionManager = mock(VideoInteractionManager::class.java)
    private val context: Context = mock(Context::class.java)
    private val sharedPrefs: SharedPreferences = mock(SharedPreferences::class.java)
    private val editor: SharedPreferences.Editor = mock(SharedPreferences.Editor::class.java)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        `when`(sharedPrefs.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.apply()).then { }
        `when`(interactionManager.interactionEvents).thenReturn(MutableSharedFlow())
        runBlocking {
            `when`(searchRepository.getHotWords()).thenReturn(Resource.Success(emptyList()))
        }
        `when`(sharedPrefs.getString(anyString(), anyString())).thenReturn("")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search success updates videoResults and userResults`() = runTest {
        val keyword = "test"
        val user = UserModel(101, "a1", "av1")
        val videos = listOf(VideoModel(1, user, "p1", "c1", "t1"))
        `when`(searchVideosUseCase.invoke(anyString(), anyLong())).thenReturn(Resource.Success(Pair(videos, 1L)))

        viewModel = SearchViewModel(searchVideosUseCase, searchUsersUseCase, searchRepository, interactionManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.search(keyword)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SearchUiState.Results, viewModel.uiState.value)
        assertEquals(videos, viewModel.videoResults.value)
    }

    @Test
    fun `loadMore appends and deduplicates videos`() = runTest {
        val keyword = "test"
        val user1 = UserModel(101, "a1", "av1")
        val user2 = UserModel(102, "a2", "av2")
        val video1 = VideoModel(1, user1, "p1", "c1", "t1")
        val video2 = VideoModel(2, user2, "p2", "c2", "t2")

        `when`(searchVideosUseCase.invoke(anyString(), eq(0L))).thenReturn(Resource.Success(Pair(listOf(video1), 1L)))
        `when`(searchVideosUseCase.invoke(anyString(), eq(1L))).thenReturn(Resource.Success(Pair(listOf(video1, video2), 2L)))

        viewModel = SearchViewModel(searchVideosUseCase, searchUsersUseCase, searchRepository, interactionManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.search(keyword)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.videoResults.value.size)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.videoResults.value.size)
        assertEquals(listOf(video1, video2), viewModel.videoResults.value)
        assertEquals(PagingState.Idle, viewModel.pagingState.value)
    }

    @Test
    fun `search with no results updates state to Empty`() = runTest {
        val keyword = "nothing"
        `when`(searchVideosUseCase.invoke(anyString(), anyLong())).thenReturn(Resource.Success(Pair(emptyList(), -1L)))

        viewModel = SearchViewModel(searchVideosUseCase, searchUsersUseCase, searchRepository, interactionManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.search(keyword)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SearchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `loadMore fails updates pagingState to Error`() = runTest {
        val keyword = "test"
        val user1 = UserModel(101, "a1", "av1")
        val video1 = VideoModel(1, user1, "p1", "c1", "t1")
        val errorMessage = "Network Fail"

        `when`(searchVideosUseCase.invoke(anyString(), eq(0L))).thenReturn(Resource.Success(Pair(listOf(video1), 1L)))
        `when`(searchVideosUseCase.invoke(anyString(), eq(1L))).thenReturn(Resource.Error(errorMessage))

        viewModel = SearchViewModel(searchVideosUseCase, searchUsersUseCase, searchRepository, interactionManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.search(keyword)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.videoResults.value.size)
        assertTrue(viewModel.pagingState.value is PagingState.Error)
        assertEquals(errorMessage, (viewModel.pagingState.value as PagingState.Error).message)
    }
}
