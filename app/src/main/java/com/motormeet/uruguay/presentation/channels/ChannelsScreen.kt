package com.motormeet.uruguay.presentation.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.ui.components.Avatar
import com.motormeet.uruguay.ui.components.VerifiedBadge
import com.motormeet.uruguay.ui.theme.*

@Composable
fun ChannelsScreen(
    onNavigateToCreateChannel: () -> Unit,
    onNavigateToChannelDetail: (String) -> Unit,
    viewModel: ChannelsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUserVerified by viewModel.isUserVerified.collectAsState()
    var showNotVerifiedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadChannels()
    }

    if (showNotVerifiedDialog) {
        AlertDialog(
            onDismissRequest = { showNotVerifiedDialog = false },
            title = { Text("Usuario no verificado") },
            text = { Text("Solo los usuarios verificados pueden crear canales para difundir eventos y novedades.") },
            confirmButton = {
                TextButton(onClick = { showNotVerifiedDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(
        containerColor = MotorBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isUserVerified) onNavigateToCreateChannel() else showNotVerifiedDialog = true
                },
                containerColor = MotorRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(17.dp)
            ) {
                Icon(Icons.Default.Add, "Crear canal")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 2.dp,
                bottom = innerPadding.calculateBottomPadding() + 95.dp,
                start = 14.dp,
                end = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("CANALES OFICIALES", color = MotorMuted, style = MaterialTheme.typography.labelSmall)
                Text("Novedades & Difusión", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 3.dp))
                Text("Canales exclusivos de organizadores verificados para enterarte de cambios y avisos.", color = MotorMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
                Spacer(Modifier.height(10.dp))
            }

            when (val state = uiState) {
                ChannelsUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MotorRed)
                    }
                }
                is ChannelsUiState.Error -> item {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                is ChannelsUiState.Success -> {
                    if (state.channels.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MotorSurface),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Campaign, null, modifier = Modifier.size(48.dp), tint = MotorMuted)
                                    Text("No hay canales disponibles aún", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                                    Text("Los organizadores verificados pueden crear canales para anunciar juntadas.", color = MotorMuted, modifier = Modifier.padding(top = 5.dp))
                                }
                            }
                        }
                    } else {
                        items(state.channels, key = { it.id }) { channel ->
                            ChannelItem(channel) { onNavigateToChannelDetail(channel.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelItem(channel: Channel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MotorSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(channel.imageUrl ?: channel.ownerPhotoUrl, channel.name, size = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(channel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (channel.isOwnerVerified) {
                        Spacer(Modifier.width(6.dp))
                        VerifiedBadge(compact = true)
                    }
                }
                if (channel.description.isNotBlank()) {
                    Text(channel.description, color = MotorMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
                }
                Text("📢 Por: ${channel.ownerName}", color = MotorMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
