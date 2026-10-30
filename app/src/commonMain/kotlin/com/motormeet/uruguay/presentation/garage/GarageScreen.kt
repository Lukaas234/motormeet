package com.motormeet.uruguay.presentation.garage

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    onNavigateBack: () -> Unit,
    onAddVehicle: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: GarageViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(MotorBlack)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = "Mi Garage",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }

        when (uiState) {
            is GarageUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f).background(MotorBlack),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MotorRed)
                }
            }
            is GarageUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(uiState.vehicles) { vehicle ->
                        VehicleItem(
                            vehicle = vehicle,
                            onClick = { onNavigateToDetail(vehicle.id) },
                            onDelete = { viewModel.deleteVehicle(vehicle.id) }
                        )
                    }
                }
            }
            is GarageUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f).background(MotorBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.message, color = MotorText)
                }
            }
        }

        Button(
            onClick = onAddVehicle,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotorRed, contentColor = Color.White)
        ) {
            Text("Agregar vehículo", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun VehicleItem(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MotorSurface),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium, color = MotorText, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${vehicle.year}", style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(vehicle.color, style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                Text(vehicle.transmission, style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                Text(vehicle.engine, style = MaterialTheme.typography.bodySmall, color = MotorMuted)
            }
        }
    }
}
