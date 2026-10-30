package com.motormeet.uruguay.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.motormeet.uruguay.presentation.auth.AuthViewModel
import com.motormeet.uruguay.ui.components.Avatar
import com.motormeet.uruguay.ui.components.VerifiedBadge
import com.motormeet.uruguay.ui.theme.*

@Composable
fun ProfileScreen(
    userId: String? = null,
    onSignOut: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPosts by viewModel.userPosts.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val publicVehicles by viewModel.publicVehicles.collectAsState()

    var showPublicVehiclesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
        if (!userId.isNullOrBlank()) {
            viewModel.loadPublicVehicles(userId)
        }
    }

    if (showPublicVehiclesDialog) {
        AlertDialog(
            onDismissRequest = { showPublicVehiclesDialog = false },
            title = { Text("Vehículos Públicos") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(publicVehicles) { vehicle ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MotorSurface2),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                if (!vehicle.photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = vehicle.photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                                Text("${vehicle.brand} ${vehicle.model} (${vehicle.year})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (vehicle.engine.isNotBlank()) Text("Motor: ${vehicle.engine}", color = MotorMuted, style = MaterialTheme.typography.bodySmall)
                                if (vehicle.modifications.isNotBlank()) {
                                    Text("Modificaciones: ${vehicle.modifications}", color = MotorText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPublicVehiclesDialog = false }) { Text("Cerrar") }
            }
        )
    }

    Scaffold(containerColor = MotorBlack) { innerPadding ->
        when (val state = uiState) {
            ProfileUiState.Loading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MotorRed) }
            is ProfileUiState.Error -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { Text(state.message, color = MaterialTheme.colorScheme.error) }
            is ProfileUiState.Success -> {
                val user = state.user
                val isOwnProfile = userId.isNullOrBlank() || user.id == currentUserId
                val hasPublicVehicles = publicVehicles.isNotEmpty()

                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(bottom = 25.dp), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    item(span = { GridItemSpan(3) }) {
                        Column {
                            Box(Modifier.fillMaxWidth().height(145.dp).background(Brush.linearGradient(listOf(Color(0xFF361018), MotorSurface2, MotorSurface))))
                            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Box(Modifier.size(82.dp).clip(CircleShape).background(MotorSurface).padding(4.dp)) { Avatar(user.photoUrl, user.displayName, Modifier.fillMaxSize(), 74.dp) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.padding(bottom = 4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) { Text(user.displayName, style = MaterialTheme.typography.titleLarge); if (user.isVerified) { Spacer(Modifier.width(7.dp)); VerifiedBadge(compact = true) } }
                                        Text(user.city.ifEmpty { user.email }, color = MotorMuted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ProfileStatItem("Posts", user.postCount); ProfileStatItem("Eventos", user.createdEventsCount); ProfileStatItem("Seguidores", user.followerCount); ProfileStatItem("Seguidos", user.followingCount)
                                }
                                if (user.bio.isNotEmpty()) Text(user.bio, color = MotorText, modifier = Modifier.padding(top = 14.dp))
                                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (isOwnProfile) {
                                        OutlinedButton(onClick = onNavigateToEditProfile, modifier = Modifier.weight(1f), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MotorText)) { Icon(Icons.Default.Edit, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("Editar perfil") }
                                        OutlinedButton(onClick = { authViewModel.signOut(); onSignOut() }, modifier = Modifier.width(52.dp), shape = RoundedCornerShape(13.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MotorRed)) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión") }
                                    } else {
                                        if (isFollowing) {
                                            OutlinedButton(
                                                onClick = { viewModel.toggleFollow(user.id) },
                                                modifier = if (hasPublicVehicles) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(13.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MotorText, containerColor = MotorSurface2)
                                            ) {
                                                Icon(Icons.Default.Check, null, Modifier.size(17.dp), tint = MotorText)
                                                Spacer(Modifier.width(6.dp))
                                                Text("Siguiendo", fontWeight = FontWeight.Bold)
                                            }

                                            if (hasPublicVehicles) {
                                                Spacer(Modifier.width(8.dp))
                                                Button(
                                                    onClick = { showPublicVehiclesDialog = true },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(13.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MotorRed, contentColor = Color.White)
                                                ) {
                                                    Icon(Icons.Default.DirectionsCar, null, Modifier.size(17.dp))
                                                    Spacer(Modifier.width(5.dp))
                                                    Text("Ver sus autos", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.toggleFollow(user.id) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(13.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MotorRed, contentColor = Color.White)
                                            ) {
                                                Text("Seguir", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(if (isOwnProfile) "Mis publicaciones" else "Publicaciones", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                    items(userPosts, key = { it.id }) { post ->
                        Card(onClick = { onNavigateToPostDetail(post.id) }, modifier = Modifier.aspectRatio(1f), shape = androidx.compose.ui.graphics.RectangleShape, colors = CardDefaults.cardColors(containerColor = MotorSurface)) {
                            if (post.imageUrl != null) AsyncImage(model = post.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Box(Modifier.fillMaxSize().padding(5.dp), contentAlignment = Alignment.Center) { Text(post.content.take(35), color = MotorMuted, style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(label: String, count: Int) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold); Text(label, style = MaterialTheme.typography.labelSmall, color = MotorMuted) } }

@Composable
fun SocialLinkItem(label: String, value: String) { Column(Modifier.fillMaxWidth()) { Text(label, style = MaterialTheme.typography.labelSmall, color = MotorMuted); Text(value, style = MaterialTheme.typography.bodyMedium, color = MotorRed, fontWeight = FontWeight.Bold) } }
