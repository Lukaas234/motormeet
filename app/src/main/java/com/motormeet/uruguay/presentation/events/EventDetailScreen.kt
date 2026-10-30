package com.motormeet.uruguay.presentation.events

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isJoining by viewModel.isJoining.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(value = false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    if (showDeleteDialog) {
        val event = (uiState as? EventDetailUiState.Success)?.event
        event?.let { e ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("¿Eliminar evento?") },
                text = { Text("Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteEvent(e.id) {
                                onNavigateBack()
                            }
                            showDeleteDialog = false
                        },
                    ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    val event = (uiState as? EventDetailUiState.Success)?.event
                    if (event != null && (event.organizerId == currentUserId)) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    IconButton(onClick = { 
                        event?.let { e ->
                            val mapsUrl = if (e.latitude != null && e.longitude != null && e.latitude != 0.0) {
                                "https://www.google.com/maps/search/?api=1&query=${e.latitude},${e.longitude}"
                            } else {
                                "https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode("${e.city}, ${e.title}")}"
                            }
                            val dateFormatted = SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("es-UY")).format(Date(e.date)).replaceFirstChar { it.uppercase() }
                            val fullDate = if (e.hour.isNotBlank()) "$dateFormatted · ${e.hour}" else dateFormatted
                            val shareText = "¡Mira este evento en MotorMeet Uruguay! 🏎️🔥\n\n📌 ${e.title}\n📅 $fullDate\n📍 ${e.city}\n\n🗺️ Ubicación en Google Maps:\n$mapsUrl"
                            
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartir evento"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is EventDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is EventDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is EventDetailUiState.Success -> {
                val event = state.event
                val formattedDate = remember(event.date, event.hour) {
                    val d = SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("es-UY")).format(Date(event.date)).replaceFirstChar { it.uppercase() }
                    if (event.hour.isNotBlank()) "$d · ${event.hour}" else d
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(event.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(event.type, modifier = Modifier.padding(4.dp))
                            }
                            if (event.price.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text(event.price, modifier = Modifier.padding(4.dp))
                                }
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    Text("Gratis", modifier = Modifier.padding(4.dp))
                                }
                            }
                        }

                        DetailIconItem(Icons.Default.CalendarMonth, formattedDate)
                        DetailIconItem(Icons.Default.LocationOn, "${event.city}, ${event.location}")

                        if (event.latitude != null && event.longitude != null && event.latitude != 0.0) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                                AndroidView(
                                    factory = { ctx ->
                                        MapView(ctx).apply {
                                            setTileSource(com.motormeet.uruguay.util.CartoVoyagerTileSource)
                                            setMultiTouchControls(true)
                                            isTilesScaledToDpi = true
                                            controller.setZoom(15.0)
                                            val position = GeoPoint(event.latitude, event.longitude)
                                            controller.setCenter(position)
                                            
                                            val marker = Marker(this)
                                            marker.position = position
                                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            marker.title = event.location
                                            overlays.add(marker)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    onRelease = { it.onDetach() }
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val gmapsUri = if (event.latitude != null && event.longitude != null && event.latitude != 0.0) {
                                    android.net.Uri.parse("geo:${event.latitude},${event.longitude}?q=${event.latitude},${event.longitude}(${android.net.Uri.encode(event.title)})")
                                } else {
                                    android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode("${event.city}, ${event.title}")}")
                                }
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmapsUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    val webUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${event.latitude ?: 0.0},${event.longitude ?: 0.0}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = com.motormeet.uruguay.ui.theme.MotorRed)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir ubicación en Google Maps", fontWeight = FontWeight.Bold)
                        }

                        DetailIconItem(Icons.Default.Person, "Organizado por: ${event.organizerName}")

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            OutlinedButton(
                                onClick = { viewModel.joinEvent(event.id) },
                                enabled = !isJoining
                            ) {
                                if (isJoining) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Asistiré (${event.attendeesCount})")
                            }
                            ActionButton(Icons.Default.Favorite, "Me gusta (${event.likesCount})") { viewModel.likeEvent(event.id) }
                        }

                        HorizontalDivider()

                        Text("Descripción", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(event.description, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailIconItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}
