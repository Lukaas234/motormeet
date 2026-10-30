package com.motormeet.uruguay.presentation.home

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
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()

    when (uiState) {
        is PostDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotorRed)
            }
        }
        is PostDetailUiState.Success -> {
            val post = uiState.post
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
                        Text(post.userName, style = MaterialTheme.typography.titleMedium, color = MotorText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(post.content, style = MaterialTheme.typography.bodyMedium, color = MotorText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { viewModel.likePost(postId) }) {
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                null,
                                tint = if (isLiked) MotorRed else MotorMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("Comentarios")

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    items(comments) { comment ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(comment.userName, style = MaterialTheme.typography.bodyMedium, color = MotorText)
                                Text(comment.content, style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        label = { Text("Escribe un comentario...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Send, null, tint = MotorRed, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        is PostDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message, color = MotorText)
            }
        }
    }
}
