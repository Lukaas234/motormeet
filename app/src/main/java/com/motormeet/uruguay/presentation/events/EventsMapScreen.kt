package com.motormeet.uruguay.presentation.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.ui.theme.MotorBlack
import com.motormeet.uruguay.ui.theme.MotorRed
import com.motormeet.uruguay.ui.theme.MotorText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsMapScreen(
    onNavigateToEventDetail: (String) -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    Scaffold(
        containerColor = MotorBlack,
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Juntadas") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MotorBlack, titleContentColor = MotorText)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is EventUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MotorRed)
                }
            }
            is EventUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is EventUiState.Success -> {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(com.motormeet.uruguay.util.CartoVoyagerTileSource)
                            setMultiTouchControls(true)
                            setBuiltInZoomControls(false)
                            isTilesScaledToDpi = true
                            controller.setZoom(7.5)
                            controller.setCenter(GeoPoint(-32.5228, -55.7658)) // Uruguay
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    update = { mapView ->
                        mapView.overlays.clear()
                        state.events.forEach { event ->
                            if (event.latitude != null && event.longitude != null) {
                                val marker = Marker(mapView)
                                marker.position = GeoPoint(event.latitude, event.longitude)
                                marker.title = event.title
                                marker.subDescription = "${event.city} - ${event.type}"
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.setOnMarkerClickListener { m, _ ->
                                    onNavigateToEventDetail(event.id)
                                    true
                                }
                                mapView.overlays.add(marker)
                            }
                        }
                        mapView.invalidate()
                    }
                )
            }
        }
    }
}
