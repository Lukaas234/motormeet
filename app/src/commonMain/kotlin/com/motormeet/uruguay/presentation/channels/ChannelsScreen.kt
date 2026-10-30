package com.motormeet.uruguay.presentation.channels

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    onNavigateToCreateChannel: () -> Unit,
    onNavigateToChannelDetail: (String) -> Unit,
    viewModel: ChannelsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Canales",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }

        when (uiState) {
            is ChannelsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f).background(MotorBlack),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MotorRed)
                }
            }
            is ChannelsUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(uiState.channels) { channel ->
                        ChannelItem(
                            channel = channel,
                            onClick = { onNavigateToChannelDetail(channel.id) }
                        )
                    }
                }
            }
            is ChannelsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f).background(MotorBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.message, color = MotorText)
                }
            }
        }

        Button(
            onClick = onNavigateToCreateChannel,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotorRed, contentColor = Color.White)
        ) {
            Text("Crear canal", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun ChannelItem(channel: Channel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MotorSurface),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(channel.name, style = MaterialTheme.typography.titleMedium, color = MotorText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(channel.description, style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
        }
    }
}
