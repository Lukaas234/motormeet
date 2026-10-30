package com.motormeet.uruguay.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    onSignOut: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: ProfileViewModel,
    authViewModel: com.motormeet.uruguay.presentation.auth.AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val publicVehicles by viewModel.publicVehicles.collectAsState()

    when (uiState) {
        is ProfileUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotorRed)
            }
        }
        is ProfileUiState.Success -> {
            val user = uiState.user
            Column(
                modifier = Modifier.fillMaxSize().background(MotorBlack)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userId == null) {
                        IconButton(onClick = { /* navigate back */ }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                    Text(
                        text = "Perfil",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 16.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(user.displayName, style = MaterialTheme.typography.headlineSmall, color = MotorText, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MotorMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { if (userId == null) onNavigateToEditProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = MotorRed, contentColor = Color.White)
                        ) {
                            Text("Editar perfil", fontWeight = FontWeight.Bold)
                        }
                        if (userId != null) {
                            Button(
                                onClick = { viewModel.toggleFollow(userId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) MotorSurface2 else MotorRed,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(if (isFollowing) "Dejar de seguir" else "Seguir")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mis vehículos", style = MaterialTheme.typography.titleLarge, color = MotorText)
                    Spacer(Modifier.weight(1f))
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(publicVehicles) { vehicle ->
                        VehicleItem(
                            vehicle = vehicle,
                            onClick = { /* navigate to detail */ },
                            onDelete = { /* delete */ }
                        )
                    }
                }

                if (userId == null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MotorSurface2, contentColor = MotorText)
                    ) {
                        Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        is ProfileUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message, color = MotorText)
            }
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
        }
    }
}
