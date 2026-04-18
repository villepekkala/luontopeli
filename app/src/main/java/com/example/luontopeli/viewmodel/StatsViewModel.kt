// 📁 viewmodel/StatsViewModel.kt
package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ViewModel tilastonäkymälle (StatsScreen).
 */
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    /** Room-tietokantainstanssi */
    private val db = AppDatabase.getDatabase(application)

    /** Kaikki kävelykerrat aikajärjestyksessä (uusin ensin) */
    private val _allSessions = MutableStateFlow<List<WalkSession>>(emptyList())
    val allSessions: StateFlow<List<WalkSession>> = _allSessions.asStateFlow()

    /** Luontolöytöjen kokonaismäärä */
    private val _totalSpots = MutableStateFlow(0)
    val totalSpots: StateFlow<Int> = _totalSpots.asStateFlow()

    init {
        // Seurataan kävelykertojen muutoksia tietokannassa
        viewModelScope.launch {
            db.walkSessionDao().getAllSessions().collect { sessions ->
                _allSessions.value = sessions
            }
        }
        // Seurataan luontolöytöjen kokonaismäärän muutoksia
        viewModelScope.launch {
            db.natureSpotDao().getAllSpots().collect { spots ->
                _totalSpots.value = spots.size
            }
        }
    }
}

// --- TÄRKEÄÄ: Lisätään ohjeen mukaiset apufunktiot tiedoston loppuun ---
// Nämä korjaavat "Unresolved reference" -virheet MapScreenissä ja DiscoverScreenissä.

/** Muuttaa etäisyyden metreistä luettavaan muotoon */
fun formatDistance(meters: Float): String {
    return if (meters >= 1000) "%.2f km".format(meters / 1000f) else "%.0f m".format(meters)
}

/** Laskee ja muotoilee keston kahden aikaleiman välillä */
fun formatDuration(startMillis: Long, endMillis: Long = System.currentTimeMillis()): String {
    val duration = endMillis - startMillis
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

/** Muuttaa aikaleiman (Long) muotoon "dd.MM.yyyy HH:mm" */
fun Long.toFormattedDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}