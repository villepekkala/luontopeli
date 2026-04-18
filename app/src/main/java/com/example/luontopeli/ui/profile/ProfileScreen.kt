package com.example.luontopeli.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luontopeli.viewmodel.StatsViewModel
import com.example.luontopeli.viewmodel.formatDistance
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    statsViewModel: StatsViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val sessions by statsViewModel.allSessions.collectAsState()
    val totalSpots by statsViewModel.totalSpots.collectAsState()

    // Lasketaan kokonaismatka kaikista sessioista
    val totalDistance = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profiili") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profiilikuva-ikoni
            Surface(
                modifier = Modifier.size(100.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Käyttäjätiedot
            Text(
                text = if (user?.isAnonymous == true) "Anonyymi seikkailija" else user?.email ?: "Ei sähköpostia",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "ID: ${user?.uid?.take(10)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tilastot-kortti
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Yhteenveto", style = MaterialTheme.typography.titleMedium)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ProfileStatRow("Kävelykerrat", "${sessions.size} kpl")
                    ProfileStatRow("Matka yhteensä", formatDistance(totalDistance))
                    ProfileStatRow("Löydetyt lajit", "$totalSpots kpl")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Kirjaudu ulos -nappi
            Button(
                onClick = {
                    auth.signOut()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Kirjaudu ulos")
            }
        }
    }
}

@Composable
fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}