package com.motormeet.uruguay.presentation.channels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import com.motormeet.uruguay.ui.components.Avatar
import com.motormeet.uruguay.ui.components.VerifiedBadge
import com.motormeet.uruguay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
    val isSending by viewModel.isSending.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(channelId) {
        viewModel.loadChannel(channelId)
    }

    Scaffold(
        containerColor = MotorBlack,
        topBar = {
            TopAppBar(
                title = {
                    val channel = (channelState as? ChannelDetailUiState.Success)?.channel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (channel != null) {
                            Avatar(channel.imageUrl ?: channel.ownerPhotoUrl, channel.name, size = 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(channel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (channel.isOwnerVerified) {
                                        Spacer(Modifier.width(4.dp))
                                        VerifiedBadge(compact = true)
                                    }
                                }
                                Text("Canal de difusión", style = MaterialTheme.typography.labelSmall, color = MotorMuted)
                            }
                        } else {
                            Text("Canal")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MotorBlack, titleContentColor = MotorText)
            )
        },
        bottomBar = {
            val channel = (channelState as? ChannelDetailUiState.Success)?.channel
            val isOwner = channel != null && channel.ownerId == currentUserId

            Surface(
                color = MotorSurface,
                tonalElevation = 6.dp
            ) {
                if (isOwner) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (selectedImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { imageLauncher.launch("image/*") }) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar foto", tint = MotorMuted)
                            }
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { Text("Difundir un aviso...", color = MotorMuted) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MotorRed,
                                    unfocusedBorderColor = MotorLine,
                                    focusedContainerColor = MotorSurface2,
                                    unfocusedContainerColor = MotorSurface2,
                                    focusedTextColor = MotorText,
                                    unfocusedTextColor = MotorText
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank() || selectedImageUri != null) {
                                        viewModel.sendMessage(channelId, messageText, selectedImageUri?.toString())
                                        messageText = ""
                                        selectedImageUri = null
                                    }
                                },
                                enabled = !isSending && (messageText.isNotBlank() || selectedImageUri != null)
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MotorRed)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = MotorRed)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MotorMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Solo el administrador puede publicar en este canal", color = MotorMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    ) { innerPadding ->
        when (val state = channelState) {
            ChannelDetailUiState.Loading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MotorRed)
            }
            is ChannelDetailUiState.Error -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is ChannelDetailUiState.Success -> {
                val channel = state.channel
                val dateFormatter = remember { SimpleDateFormat("HH:mm · dd MMM", Locale.forLanguageTag("es-UY")) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MotorSurface2),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Campaign, null, tint = MotorRed, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Información del Canal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                if (channel.description.isNotBlank()) {
                                    Text(channel.description, color = MotorMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                                }
                                Text("Creado por ${channel.ownerName}", color = MotorMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                    }

                    if (messages.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Aún no hay anuncios publicados en este canal.", color = MotorMuted)
                            }
                        }
                    } else {
                        items(messages, key = { it.id }) { msg ->
                            val isOwner = channel.ownerId == currentUserId
                            ChannelMessageBubble(
                                message = msg,
                                timeText = dateFormatter.format(Date(msg.createdAt)),
                                isOwner = isOwner,
                                onDelete = { viewModel.deleteMessage(channelId, msg.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelMessageBubble(
    message: ChannelMessage,
    timeText: String,
    isOwner: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar mensaje?") },
            text = { Text("Este mensaje dejará de ser visible en el canal.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MotorSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message.senderName, color = MotorRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                if (isOwner) {
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar mensaje", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            if (message.content.isNotBlank()) {
                Text(message.content, color = MotorText, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
            }
            if (!message.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(timeText, color = MotorMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End))
        }
    }
}
