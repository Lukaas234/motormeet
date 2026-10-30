package com.motormeet.uruguay.presentation.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.ui.theme.MotorBlack
import com.motormeet.uruguay.ui.theme.MotorLine
import com.motormeet.uruguay.ui.theme.MotorMuted
import com.motormeet.uruguay.ui.theme.MotorRed
import com.motormeet.uruguay.ui.theme.MotorSurface
import com.motormeet.uruguay.ui.theme.MotorSurface2
import com.motormeet.uruguay.ui.theme.MotorText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventListScreen(onNavigateToCreateEvent: () -> Unit, onNavigateToEventDetail: (String) -> Unit, viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isUserVerified by viewModel.isUserVerified.collectAsState()
    var showNotVerifiedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    if (showNotVerifiedDialog) AlertDialog(
        onDismissRequest = { showNotVerifiedDialog = false },
        title = { Text("Usuario no verificado") },
        text = { Text("Solo los usuarios verificados por la administración pueden crear eventos para asegurar la calidad de las juntadas.") },
        confirmButton = { TextButton(onClick = { showNotVerifiedDialog = false }) { Text("Entendido") } }
    )

    Scaffold(
        containerColor = MotorBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (isUserVerified) onNavigateToCreateEvent() else showNotVerifiedDialog = true },
                containerColor = MotorRed, contentColor = Color.White, shape = RoundedCornerShape(17.dp)
            ) { Icon(Icons.Default.Add, "Crear evento") }
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 2.dp,
                bottom = innerPadding.calculateBottomPadding() + 95.dp,
                start = 14.dp,
                end = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("CALENDARIO", color = MotorMuted, style = MaterialTheme.typography.labelSmall)
                Text("Próximas Juntadas", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 3.dp))
                Text("Encontrá eventos de autos y motos en Uruguay.", color = MotorMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Todas") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Autos") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Motos") })
                }
                Spacer(Modifier.height(4.dp))
            }
            when (val state = uiState) {
                EventUiState.Loading -> item { Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MotorRed) } }
                is EventUiState.Error -> item { Text(state.message, color = MaterialTheme.colorScheme.error) }
                is EventUiState.Success -> {
                    if (state.events.isEmpty()) item { Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) { Text("No hay juntadas programadas próximamente.", color = MotorMuted) } }
                    else items(state.events, key = { it.id }) { event -> EventItem(event) { onNavigateToEventDetail(event.id) } }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    val dateString = remember(event.date, event.hour) {
        val d = SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("es-UY")).format(Date(event.date)).replaceFirstChar { it.uppercase() }
        if (event.hour.isNotBlank()) "$d · ${event.hour}" else d
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MotorSurface), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)) {
        Column {
            Box(Modifier.fillMaxWidth().height(118.dp).background(Brush.linearGradient(listOf(Color(0xFF252B36), Color(0xFF111318))))) {
                Text("🏎️", fontSize = 58.sp, modifier = Modifier.align(Alignment.Center))
                Surface(Modifier.align(Alignment.TopEnd).padding(10.dp), color = Color.Black.copy(alpha = .55f), shape = RoundedCornerShape(9.dp)) { Text("JUNTADA", Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Color.White, style = MaterialTheme.typography.labelSmall) }
            }
            Column(Modifier.padding(15.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarMonth, null, Modifier.size(17.dp), tint = MotorRed); Spacer(Modifier.width(7.dp)); Text(dateString, color = MotorText, style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, Modifier.size(17.dp), tint = MotorRed); Spacer(Modifier.width(7.dp)); Text("${event.city}, ${event.location}", color = MotorMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1) }
                if (event.description.isNotBlank()) Text(event.description, color = MotorMuted, style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.padding(top = 9.dp))
            }
        }
    }
}
