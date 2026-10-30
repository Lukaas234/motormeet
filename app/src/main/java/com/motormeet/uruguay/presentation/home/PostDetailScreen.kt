package com.motormeet.uruguay.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.motormeet.uruguay.domain.model.PostComment
import com.motormeet.uruguay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Scaffold(
        containerColor = MotorBlack,
        topBar = {
            TopAppBar(
                title = { Text("Publicación", color = MotorText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MotorText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MotorBlack)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is PostDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MotorRed)
                }
            }
            is PostDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is PostDetailUiState.Success -> {
                val post = state.post
                val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!post.userPhotoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = post.userPhotoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = MotorMuted)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(post.userName, fontWeight = FontWeight.Bold, color = MotorText)
                                        if (post.isUserVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verificado",
                                                tint = Color(0xFF2196F3),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(dateFormatter.format(Date(post.createdAt)), style = MaterialTheme.typography.bodySmall, color = MotorMuted)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(post.content, style = MaterialTheme.typography.bodyLarge, color = MotorText)
                            
                            if (post.imageUrl != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                AsyncImage(
                                    model = post.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.likePost(post.id) }) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Me gusta", tint = if (isLiked) MotorRed else MotorMuted)
                                }
                                Text("${post.likesCount} Me gusta", fontWeight = FontWeight.Bold, color = MotorText)
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, tint = MotorMuted)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${post.commentsCount} comentarios", color = MotorMuted)
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MotorLine)
                            Text("Comentarios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MotorText)
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Añadir un comentario...", color = MotorMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MotorRed,
                                    unfocusedBorderColor = MotorLine,
                                    focusedContainerColor = MotorSurface,
                                    unfocusedContainerColor = MotorSurface,
                                    focusedTextColor = MotorText,
                                    unfocusedTextColor = MotorText
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (commentText.isNotBlank()) {
                                                viewModel.addComment(post.id, commentText)
                                                commentText = ""
                                            }
                                        },
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = MotorRed)
                                    }
                                }
                            )
                        }
                    }

                    items(comments) { comment ->
                        PostCommentItem(comment)
                    }
                }
            }
        }
    }
}

@Composable
fun PostCommentItem(comment: PostComment) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp)) {
        if (!comment.userPhotoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = comment.userPhotoUrl,
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(32.dp), tint = MotorMuted)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MotorText)
                if (comment.isUserVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium, color = MotorText)
        }
    }
}
