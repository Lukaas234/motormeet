package com.motormeet.uruguay.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.sp
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.ui.components.Avatar
import com.motormeet.uruguay.ui.components.MotorMeetLogo
import com.motormeet.uruguay.ui.components.SectionHeader
import com.motormeet.uruguay.ui.components.VerifiedBadge
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGarage: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToEvents: () -> Unit,
    viewModel: HomeViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MotorBlack,
        topBar = {
            Column(Modifier.background(MotorBlack)) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MotorMeetLogo(Modifier.size(42.dp))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar...", color = MotorMuted, style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MotorMuted, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if (searchQuery.isNotEmpty()) IconButton({ viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) } },
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MotorRed,
                            unfocusedBorderColor = MotorLine,
                            focusedContainerColor = MotorSurface,
                            unfocusedContainerColor = MotorSurface,
                            focusedTextColor = MotorText,
                            unfocusedTextColor = MotorText
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { /* Notifications */ }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.NotificationsNone, "Notificaciones", tint = MotorText, modifier = Modifier.size(22.dp))
                    }
                }
                if (searchQuery.isNotEmpty() && isSearching) {
                    LazyColumn(
                        Modifier.fillMaxWidth().padding(14.dp)
                    ) {
                        items(searchResults) { user ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(user.photoUrl, user.displayName.take(1).uppercase(), size = 40.dp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(user.displayName, style = MaterialTheme.typography.bodyMedium, color = MotorText)
                                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(MotorBlack),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MotorRed)
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    items(uiState.posts) { post ->
                        PostItem(
                            post = post,
                            onClick = { viewModel.onSearchQueryChange("") },
                            onDelete = { viewModel.deletePost(post.id) },
                            onLike = { viewModel.likePost(post.id) }
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(MotorBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.message, color = MotorText)
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLike: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MotorSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Avatar(null, post.userName.take(1).uppercase(), size = 36.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.userName, style = MaterialTheme.typography.bodyMedium, color = MotorText, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MotorMuted, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(post.content, style = MaterialTheme.typography.bodyMedium, color = MotorText)
            post.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MotorSurface2)
                ) {
                    // Image would be loaded here with Coil or similar
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onLike) {
                    Icon(Icons.Default.Favorite, null, tint = MotorRed, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Comment, null, tint = MotorMuted, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, null, tint = MotorMuted, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
