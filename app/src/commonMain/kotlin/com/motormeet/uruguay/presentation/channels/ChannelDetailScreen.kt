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
import com.motormeet.uruguay.domain.model.ChannelMessage
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
    channelId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChannelDetailViewModel
) {
    val channelState by viewModel.channelState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    var messageContent by remember { mutableStateOf("") }

    when (channelState) {
        is ChannelDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotorRed)
            }
        }
        is ChannelDetailUiState.Success -> {
            val channel = channelState.channel
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
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MotorText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 16.dp)
                ) {
                    items(messages) { msg ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = if (msg.senderId == currentUserId) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(msg.senderName, style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                                Text(
                                    msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MotorText,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                        .background(if (msg.senderId == currentUserId) MotorRed else MotorSurface2)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageContent,
                        onValueChange = { messageContent = it },
                        label = { Text("Escribe un mensaje...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.sendMessage(channelId, messageContent) }) {
                        Icon(Icons.Default.Send, null, tint = MotorRed, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        is ChannelDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(channelState.message, color = MotorText)
            }
        }
    }
}
