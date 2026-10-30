package com.motormeet.uruguay.presentation.events

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsMapScreen(
    onNavigateToEventDetail: (String) -> Unit,
    viewModel: EventViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MotorBlack)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* navigate back */ }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = "Mapa de eventos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().weight(1f).background(MotorSurface2)
        ) {
            Text("Mapa disponible solo en Android", color = MotorMuted, modifier = Modifier.align(Alignment.Center))
        }
    }
}
