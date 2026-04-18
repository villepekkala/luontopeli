package com.example.luontopeli.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Map : Screen("map", "Kartta", Icons.Default.LocationOn)
    object Camera : Screen("camera", "Kamera", Icons.Default.AddCircle) // Muutettu icon selkeyden vuoksi
    object Discover : Screen("discover", "Löydöt", Icons.Default.Search)
    object Stats : Screen("stats", "Tilastot", Icons.Default.Info)
    // LISÄTÄÄN TÄMÄ:
    object Profile : Screen("profile", "Profiili", Icons.Default.Person)

    companion object {
        // Alapalkkiin tulevat sivut (voit päättää haluatko Profiilin listaan vai erilliseksi)
        val bottomNavScreens = listOf(Map, Camera, Discover, Stats, Profile)
    }
}