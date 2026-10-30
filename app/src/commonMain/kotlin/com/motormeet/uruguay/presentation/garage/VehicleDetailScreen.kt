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
fun VehicleDetailScreen(
    vehicleId: String,
    onNavigateBack: () -> Unit,
    viewModel: VehicleDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    when (uiState) {
        is VehicleDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotorRed)
            }
        }
        is VehicleDetailUiState.Success -> {
            val vehicle = uiState.vehicle
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
                        Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.headlineSmall, color = MotorText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Año: ${vehicle.year}", style = MaterialTheme.typography.bodyMedium, color = MotorText)
                            Spacer(Modifier.width(16.dp))
                            Text("Color: ${vehicle.color}", style = MaterialTheme.typography.bodyMedium, color = MotorText)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Motor: ${vehicle.engine}", style = MaterialTheme.typography.bodyMedium, color = MotorText)
                            Spacer(Modifier.width(16.dp))
                            Text("Transmisión: ${vehicle.transmission}", style = MaterialTheme.typography.bodyMedium, color = MotorText)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(vehicle.description, style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Pública", style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.updateVehicleVisibility(vehicle, !vehicle.isPublic) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (vehicle.isPublic) MotorRed else MotorSurface2,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (vehicle.isPublic) "Hacer privada" else "Hacer pública")
                    }
                }
            }
        }
        is VehicleDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message, color = MotorText)
            }
        }
    }
}
