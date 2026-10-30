package com.motormeet.uruguay.presentation.garage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.ui.theme.MotorBlack
import com.motormeet.uruguay.ui.theme.MotorLine
import com.motormeet.uruguay.ui.theme.MotorMuted
import com.motormeet.uruguay.ui.theme.MotorRed
import com.motormeet.uruguay.ui.theme.MotorSurface
import com.motormeet.uruguay.ui.theme.MotorText

@Composable
fun GarageScreen(onNavigateBack: () -> Unit, onAddVehicle: () -> Unit, onNavigateToDetail: (String) -> Unit, viewModel: GarageViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    Scaffold(containerColor = MotorBlack, floatingActionButton = {
        FloatingActionButton(onClick = onAddVehicle, containerColor = MotorRed, contentColor = Color.White, shape = RoundedCornerShape(17.dp)) { Icon(Icons.Default.Add, "Agregar vehículo") }
    }) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 2.dp,
                bottom = innerPadding.calculateBottomPadding() + 14.dp,
                start = 14.dp,
                end = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("TU COLECCIÓN", color = MotorMuted, style = MaterialTheme.typography.labelSmall)
                Text("Mi Garage", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 3.dp))
                Text("Mostrá tus fierros y mantené todos tus vehículos en un solo lugar.", color = MotorMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
                Spacer(Modifier.height(10.dp))
            }
            when (val state = uiState) {
                GarageUiState.Loading -> item { Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MotorRed) } }
                is GarageUiState.Error -> item { Text(state.message, color = MaterialTheme.colorScheme.error) }
                is GarageUiState.Success -> if (state.vehicles.isEmpty()) item { EmptyGarage(onAddVehicle) } else items(state.vehicles, key = { it.id }) { vehicle -> VehicleItem(vehicle, currentUserId, { onNavigateToDetail(vehicle.id) }, { viewModel.deleteVehicle(vehicle.id) }) }
            }
        }
    }
}

@Composable
private fun EmptyGarage(onAddVehicle: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MotorSurface), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🚗", style = MaterialTheme.typography.headlineSmall)
            Text("Tu garage está vacío", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            Text("Agregá tu primer vehículo para empezar a mostrarlo.", color = MotorMuted, modifier = Modifier.padding(top = 5.dp))
            Button(onClick = onAddVehicle, colors = ButtonDefaults.buttonColors(containerColor = MotorRed), modifier = Modifier.padding(top = 15.dp), shape = RoundedCornerShape(13.dp)) { Text("Agregar vehículo") }
        }
    }
}

@Composable
fun VehicleItem(vehicle: Vehicle, currentUserId: String?, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MotorSurface), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).background(Brush.linearGradient(listOf(Color(0xFF252B36), Color(0xFF111318))))) {
                if (vehicle.photoUrl != null) AsyncImage(model = vehicle.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text("🚗", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.headlineSmall)
                if (vehicle.ownerId == currentUserId) IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Delete, "Eliminar", tint = Color.White) }
            }
            Column(Modifier.padding(15.dp)) {
                Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleLarge)
                Text(vehicle.year.toString(), color = MotorMuted, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
    if (showDeleteDialog) AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("¿Eliminar vehículo?") }, text = { Text("Esta acción no se puede deshacer.") }, confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } })
}
