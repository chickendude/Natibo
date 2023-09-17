package ch.ralena.natibo.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class Timer @Inject constructor(private val timeManager: TimeManager) {
    private var elapsedMs: Long = 0
    private var startTimeMs: Long = 0
    private var _isPaused = MutableStateFlow(true)
    val isPaused get() = _isPaused.asStateFlow()

    val elapsedTimeMs: Long
        get() = if (_isPaused.value) elapsedMs else elapsedMs + timeManager.currentTimeMs() - startTimeMs

    fun reset() {
        elapsedMs = 0
        startTimeMs = timeManager.currentTimeMs()
    }

    fun start() {
        _isPaused.value = false
        startTimeMs = timeManager.currentTimeMs()
    }

    fun pause() {
        if (!_isPaused.value) {
            _isPaused.value = true
            elapsedMs += timeManager.currentTimeMs() - startTimeMs
        }
    }
}

interface TimeManager {
    fun currentTimeMs(): Long
}

class TimeManagerImpl @Inject constructor() : TimeManager {
    override fun currentTimeMs(): Long = System.currentTimeMillis()
}
