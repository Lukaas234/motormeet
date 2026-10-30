package com.motormeet.uruguay.presentation.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateEventViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var hour by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Autos") }
    var price by remember { mutableStateOf("") }
    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    val eventTypes = listOf("Autos", "Motos", "Multimarca", "JDM", "Euro", "Muscle", "Drift", "Clásicos", "Track Day", "RR", "Todos bienvenidos")

    val uiState by viewModel.uiState.collectAsState()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        // Adjust UTC midnight from DatePicker to local timezone
                        val tz = TimeZone.getDefault()
                        val offset = tz.getOffset(utcMillis)
                        date = utcMillis - offset
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is CreateEventUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del Evento") },
                modifier = Modifier.fillMaxWidth()
            )

            // Event Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = !showTypeMenu }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Evento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    eventTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                type = selectionOption
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad / Departamento") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text("Hora (HH:mm)") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            val dateFormatter = remember { SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("es-UY")) }
            val formattedDateText = remember(date, hour) {
                val d = dateFormatter.format(Date(date)).replaceFirstChar { it.uppercase() }
                if (hour.isNotBlank()) "$d · $hour" else d
            }

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Fecha: $formattedDateText", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Text("Seleccionar Ubicación exacta en el mapa", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            
            Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp))) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(com.motormeet.uruguay.util.CartoVoyagerTileSource)
                            setMultiTouchControls(true)
                            isTilesScaledToDpi = true
                            controller.setZoom(7.0)
                            controller.setCenter(GeoPoint(-32.5228, -55.7658)) // Uruguay
                            
                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    selectedGeoPoint = p
                                    overlays.filterIsInstance<Marker>().forEach { overlays.remove(it) }
                                    p?.let {
                                        val marker = Marker(this@apply)
                                        marker.position = it
                                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        overlays.add(marker)
                                    }
                                    invalidate()
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint?): Boolean = false
                            })
                            overlays.add(eventsOverlay)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mapView ->
                        selectedGeoPoint?.let { 
                            mapView.overlays.filterIsInstance<Marker>().forEach { mapView.overlays.remove(it) }
                            val marker = Marker(mapView)
                            marker.position = it
                            mapView.overlays.add(marker)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    viewModel.createEvent(
                        title = title,
                        description = description,
                        location = city,
                        city = city,
                        date = date,
                        hour = hour,
                        type = type,
                        price = price,
                        latitude = selectedGeoPoint?.latitude,
                        longitude = selectedGeoPoint?.longitude
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateEventUiState.Loading && title.isNotBlank()
            ) {
                if (uiState is CreateEventUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear Evento")
                }
            }

            if (uiState is CreateEventUiState.Error) {
                Text(
                    text = (uiState as CreateEventUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
