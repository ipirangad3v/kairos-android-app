package digital.tonima.core.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import digital.tonima.core.delegates.ProUserProvider
import digital.tonima.core.permissions.PermissionManager
import digital.tonima.core.repository.AppPreferencesRepository
import digital.tonima.core.repository.AudioWarningState
import digital.tonima.core.repository.CalendarRepository
import digital.tonima.core.repository.RingerModeRepository
import digital.tonima.core.service.EventAlarmScheduler
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class EventViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val mockProUserProvider: ProUserProvider = mockk(relaxed = true)
    private val mockCalendarRepository: CalendarRepository = mockk(relaxed = true)
    private val mockAppPreferencesRepository: AppPreferencesRepository = mockk(relaxed = true)
    private val mockRingerModeRepository: RingerModeRepository = mockk(relaxed = true)
    private val mockScheduler: EventAlarmScheduler = mockk(relaxed = true)
    private val mockPermissionManager: PermissionManager = mockk(relaxed = true)
    private lateinit var viewModel: EventViewModel

    private val isGlobalAlarmEnabledFlow = MutableStateFlow(true)
    private val autostartSuggestionDismissedFlow = MutableStateFlow(false)
    private val disabledEventIdsFlow = MutableStateFlow(emptySet<String>())
    private val ringerModeFlow = MutableStateFlow(AudioWarningState.NORMAL)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { mockAppPreferencesRepository.isGlobalAlarmEnabled() } returns isGlobalAlarmEnabledFlow
        every { mockAppPreferencesRepository.getAutostartSuggestionDismissed() } returns autostartSuggestionDismissedFlow
        every { mockAppPreferencesRepository.getDisabledEventIds() } returns disabledEventIdsFlow
        every { mockRingerModeRepository.ringerMode } returns ringerModeFlow
        every { mockRingerModeRepository.startObserving() } just Runs
        every { mockRingerModeRepository.stopObserving() } just Runs

        every { mockPermissionManager.hasCalendarPermission() } returns true
        every { mockPermissionManager.hasPostNotificationsPermission() } returns true
        every { mockPermissionManager.hasExactAlarmPermission() } returns true
        every { mockPermissionManager.hasFullScreenIntentPermission() } returns true

        viewModel = EventViewModel(
            mockProUserProvider,
            mockCalendarRepository,
            mockAppPreferencesRepository,
            mockRingerModeRepository,
            mockScheduler,
            mockPermissionManager,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCleared calls stopObserving on RingerModeRepository`() = runTest {
        viewModel.onCleared()
        verify(exactly = 1) { mockRingerModeRepository.stopObserving() }
    }

    @Test
    fun `checkAllPermissions updates all permission flags in UI state`() = runTest {
        every { mockPermissionManager.hasCalendarPermission() } returns false
        every { mockPermissionManager.hasPostNotificationsPermission() } returns false

        viewModel.uiState.test {
            skipItems(1)

            viewModel.checkAllPermissions()
            advanceUntilIdle()

            val updatedState = awaitItem()
            assertFalse(updatedState.hasCalendarPermission)
            assertFalse(updatedState.hasPostNotificationsPermission)
            assertTrue(updatedState.hasExactAlarmPermission)
            assertTrue(updatedState.hasFullScreenIntentPermission)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `dismissAutostartSuggestion sets dismissed flag in preferences`() = runTest {
        coEvery { mockAppPreferencesRepository.setAutostartSuggestionDismissed(true) } just Runs

        viewModel.dismissAutostartSuggestion()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockAppPreferencesRepository.setAutostartSuggestionDismissed(true) }
    }

    @Test
    fun `onDateSelected updates selectedDate in UI state`() = runTest {
        val newDate = LocalDate.of(2023, 10, 26)

        viewModel.uiState.test {
            skipItems(1)

            viewModel.onDateSelected(newDate)
            advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(newDate, updatedState.selectedDate)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onAlarmsToggle sets global alarm enabled status in preferences`() = runTest {
        coEvery { mockAppPreferencesRepository.setGlobalAlarmEnabled(any()) } just Runs

        viewModel.onAlarmsToggle(false)
        advanceUntilIdle()
        coVerify(exactly = 1) { mockAppPreferencesRepository.setGlobalAlarmEnabled(false) }

        viewModel.onAlarmsToggle(true)
        advanceUntilIdle()
        coVerify(exactly = 1) { mockAppPreferencesRepository.setGlobalAlarmEnabled(true) }
    }

    @Test
    fun `onUpgradeToProRequest updates showUpgradeConfirmation to true`() = runTest {
        viewModel.uiState.test {
            skipItems(1)

            viewModel.onUpgradeToProRequest()
            advanceUntilIdle()

            val updatedState = awaitItem()
            assertTrue(updatedState.showUpgradeConfirmation)
            cancelAndConsumeRemainingEvents()
        }
    }
}
