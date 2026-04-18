// 📁 viewmodel/WalkViewModel.kt
package com.example.luontopeli.viewmodel

import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.sensor.StepCounterManager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel kävelyn hallintaan.
 */
class WalkViewModel(application: Application) : AndroidViewModel(application) {

    private val stepManager = StepCounterManager(application)
    private val db = AppDatabase.getDatabase(application)

    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    fun startWalk() {
        if (_isWalking.value) return

        val session = WalkSession()
        _currentSession.value = session
        _isWalking.value = true

        stepManager.startStepCounting {
            _currentSession.update { current ->
                current?.copy(
                    stepCount = current.stepCount + 1,
                    distanceMeters = current.distanceMeters + StepCounterManager.STEP_LENGTH_METERS
                )
            }
        }
    }

    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false

        _currentSession.update { it?.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )}

        viewModelScope.launch {
            _currentSession.value?.let { session ->
                db.walkSessionDao().insert(session)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}
// TÄSTÄ ALASPÄIN KAIKKI POISTETTU, KOSKA NE OVAT JO STATSVIEWMODELISSA!