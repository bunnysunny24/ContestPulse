package com.bunny.contestpulse.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bunny.contestpulse.alarm.AlarmScheduler
import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.data.model.Platform
import com.bunny.contestpulse.data.repository.ContestRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ContestUiState(
    val contests: List<Contest> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedPlatform: Platform? = null
)

class ContestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContestRepository.getInstance(application)

    private val _uiState = MutableStateFlow(ContestUiState())
    val uiState: StateFlow<ContestUiState> = _uiState.asStateFlow()

    init {
        observeContests()
        refreshContests()
    }

    private fun observeContests() {
        viewModelScope.launch {
            repository.getUpcomingContests().collect { contests ->
                _uiState.update {
                    it.copy(
                        contests = contests,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshContests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                val success = repository.refreshContests()
                if (success) {
                    AlarmScheduler.scheduleAllAlarms(getApplication())
                }
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = if (!success) "Some platforms couldn't be reached" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = "Failed to refresh: ${e.message}"
                    )
                }
            }
        }
    }

    fun filterByPlatform(platform: Platform?) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    fun toggleAlarm(contestId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarm(contestId, enabled)
            if (enabled) {
                val contest = _uiState.value.contests.find { it.id == contestId }
                if (contest != null) {
                    com.bunny.contestpulse.data.model.AlarmType.entries.forEach { alarmType ->
                        AlarmScheduler.scheduleAlarm(
                            getApplication(),
                            contest.id,
                            contest.name,
                            contest.startTimeMillis,
                            alarmType
                        )
                    }
                }
            } else {
                AlarmScheduler.cancelAlarms(getApplication(), contestId)
            }
            com.bunny.contestpulse.widget.WidgetUpdater.updateWidgets(getApplication())
        }
    }

    fun getFilteredContests(): List<Contest> {
        val state = _uiState.value
        return if (state.selectedPlatform != null) {
            state.contests.filter { it.platform == state.selectedPlatform }
        } else {
            state.contests
        }
    }
}
