package com.app.douyin.pro.feature.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.feature.profile.domain.usecase.GetProfileInfoUseCase
import com.app.douyin.pro.feature.profile.domain.usecase.GetPublishedVideosUseCase
import com.app.douyin.pro.lib.media.auth.AuthManager
import com.app.douyin.pro.lib.media.auth.AuthRepository
import com.app.douyin.pro.lib.media.auth.SessionState
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.VideoModel
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.network.model.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val getProfileInfoUseCase: GetProfileInfoUseCase = mock(GetProfileInfoUseCase::class.java)
    private val getPublishedVideosUseCase: GetPublishedVideosUseCase = mock(GetPublishedVideosUseCase::class.java)
    private val authRepository: AuthRepository = mock(AuthRepository::class.java)
    private val authManager: AuthManager = mock(AuthManager::class.java)
    private val interactionManager: VideoInteractionManager = mock(VideoInteractionManager::class.java)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val userDto = UserDto(123L, "test", 0, 0, false, null, null, null)
        `when`(authRepository.getSessionState()).thenReturn(MutableStateFlow(SessionState.LoggedIn(userDto)))
        `when`(interactionManager.interactionEvents).thenReturn(MutableSharedFlow())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewMode should be SELF when userId is null`() = runTest {
        `when`(authManager.getUserId()).thenReturn(123L)
        val savedStateHandle = SavedStateHandle()

        val viewModel = ProfileViewModel(
            getProfileInfoUseCase,
            getPublishedVideosUseCase,
            authRepository,
            authManager,
            interactionManager,
            savedStateHandle
        )

        assertEquals(ProfileViewMode.SELF, viewModel.viewMode.value)
    }

    @Test
    fun `viewMode should be SELF when userId matches current user`() = runTest {
        `when`(authManager.getUserId()).thenReturn(123L)
        val savedStateHandle = SavedStateHandle(mapOf("userId" to 123L))

        val viewModel = ProfileViewModel(
            getProfileInfoUseCase,
            getPublishedVideosUseCase,
            authRepository,
            authManager,
            interactionManager,
            savedStateHandle
        )

        assertEquals(ProfileViewMode.SELF, viewModel.viewMode.value)
    }

    @Test
    fun `viewMode should be VISITOR when userId is different`() = runTest {
        `when`(authManager.getUserId()).thenReturn(123L)
        val savedStateHandle = SavedStateHandle(mapOf("userId" to 456L))

        val viewModel = ProfileViewModel(
            getProfileInfoUseCase,
            getPublishedVideosUseCase,
            authRepository,
            authManager,
            interactionManager,
            savedStateHandle
        )

        assertEquals(ProfileViewMode.VISITOR, viewModel.viewMode.value)
    }

    @Test
    fun `loadProfile should update workCount and favoritedCount`() = runTest {
        val userId = 123L
        `when`(authManager.getUserId()).thenReturn(userId)
        val savedStateHandle = SavedStateHandle(mapOf("userId" to userId))

        val user = UserModel(userId, "test", "avatar")
        val videos = listOf(
            VideoModel(1, user, "p1", "c1", "t1", likeCount = 10),
            VideoModel(2, user, "p2", "c2", "t2", likeCount = 20)
        )
        val profileInfo = ProfileInfo(userId, "test", "dy_123", 0, "0", 0, false, "0")

        `when`(getPublishedVideosUseCase(userId)).thenReturn(Resource.Success(videos))
        `when`(getProfileInfoUseCase(userId)).thenReturn(Resource.Success(profileInfo))

        val viewModel = ProfileViewModel(
            getProfileInfoUseCase,
            getPublishedVideosUseCase,
            authRepository,
            authManager,
            interactionManager,
            savedStateHandle
        )

        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val resultInfo = viewModel.profileInfo.value
        assertEquals(2, resultInfo?.workCount)
        assertEquals(30L, resultInfo?.favoritedCount)
    }
}
