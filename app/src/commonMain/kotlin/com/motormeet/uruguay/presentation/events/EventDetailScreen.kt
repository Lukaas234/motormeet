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
import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasAttended by viewModel.hasAttended.collectAsState()
    val hasLiked by viewModel.hasLiked.collectAsState()

    when (uiState) {
        is EventDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotorRed)
            }
        }
        is EventDetailUiState.Success -> {
            val event = uiState.event
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MotorBlack)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MotorSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(event.title, style = MaterialTheme.typography.headlineSmall, color = MotorText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(event.description, style = MaterialTheme.typography.bodyMedium, color = MotorText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(event.date.toString(), style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
                            Text(event.hour, style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
                            Text(event.location, style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.joinEvent(eventId) },
                        enabled = !viewModel.isJoining.value,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasAttended) MotorSurface2 else MotorRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (hasAttended) "Ya asistes" else "Asistir")
                    }
                    IconButton(onClick = { viewModel.likeEvent(eventId) }) {
                        Icon(
                            if (hasLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null,
                            tint = if (hasLiked) MotorRed else MotorMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (viewModel.isJoining.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MotorRed)
                }
            }
        }
        is EventDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message, color = MotorText)
            }
        }
    }
}
