package com.motormeet.uruguay.presentation.home

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.ui.components.Avatar
import com.motormeet.uruguay.ui.components.MotorMeetLogo
import com.motormeet.uruguay.ui.components.SectionHeader
import com.motormeet.uruguay.ui.components.VerifiedBadge
import com.motormeet.uruguay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGarage: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToEvents: () -> Unit,
    viewModel: HomeViewModel,
    eventViewModel: com.motormeet.uruguay.presentation.events.EventViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val eventState by eventViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
        eventViewModel.loadEvents()
    }

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
                        placeholder = { Text("Buscar...", color = MotorMuted, style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MotorMuted, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if (searchQuery.isNotEmpty()) IconButton({ viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) } },
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MotorRed, unfocusedBorderColor = MotorLine, focusedContainerColor = MotorSurface, unfocusedContainerColor = MotorSurface, focusedTextColor = MotorText, unfocusedTextColor = MotorText)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        Toast.makeText(context, "No tienes notificaciones nuevas", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.NotificationsNone, "Notificaciones", tint = MotorText, modifier = Modifier.size(22.dp))
                    }
                }
                if (searchQuery.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth().padding(14.dp), colors = CardDefaults.cardColors(containerColor = MotorSurface2), shape = RoundedCornerShape(16.dp)) {
                        if (isSearching) Box(Modifier.fillMaxWidth().padding(18.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MotorRed) }
                        else if (searchResults.isEmpty()) Text("No encontramos usuarios con esa búsqueda.", Modifier.padding(16.dp), color = MotorMuted)
                        else Column { searchResults.take(5).forEach { user -> ListItem(headlineContent = { Row(verticalAlignment = Alignment.CenterVertically) { Text(user.displayName); if (user.isVerified) { Spacer(Modifier.width(5.dp)); VerifiedBadge(compact = true) } } }, supportingContent = { Text(user.city) }, leadingContent = { Avatar(user.photoUrl, user.displayName, size = 40.dp) }, modifier = Modifier.clickable { onNavigateToUserProfile(user.id) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent)) } }
                    }
                }
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = onNavigateToCreatePost, containerColor = MotorRed, contentColor = Color.White, shape = RoundedCornerShape(17.dp)) { Icon(Icons.Default.Add, "Crear publicación") } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()), contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            item { SectionHeader("🔥 Próximas juntadas", "Ver todas", onNavigateToEvents) }
            when (eventState) {
                is com.motormeet.uruguay.presentation.events.EventUiState.Loading -> item { Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MotorRed) } }
                is com.motormeet.uruguay.presentation.events.EventUiState.Error -> item { Text((eventState as com.motormeet.uruguay.presentation.events.EventUiState.Error).message, color = MaterialTheme.colorScheme.error) }
                is com.motormeet.uruguay.presentation.events.EventUiState.Success -> item {
                    val events = (eventState as com.motormeet.uruguay.presentation.events.EventUiState.Success).events
                    if (events.isEmpty()) Text("Todavía no hay juntadas próximas.", color = MotorMuted)
                    else LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) { items(events.take(6)) { event -> HomeEventCard(event, onNavigateToEvents) } }
                }
            }
            item { SectionHeader("📸 Comunidad") }
            when (val state = uiState) {
                is HomeUiState.Loading -> item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MotorRed) } }
                is HomeUiState.Error -> item { Text(state.message, color = MaterialTheme.colorScheme.error) }
                is HomeUiState.Success -> if (state.posts.isEmpty()) item { Text("No hay publicaciones aún. ¡Sé el primero!", color = MotorMuted) } else items(state.posts) { post ->
                    val isLiked = viewModel.isPostLiked(post.id)
                    PostItem(
                        post = post,
                        currentUserId = currentUserId,
                        onClick = { onNavigateToPostDetail(post.id) },
                        onDelete = { viewModel.deletePost(post.id) },
                        onLike = { viewModel.likePost(post.id) },
                        isLiked = isLiked
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCard(icon: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(86.dp), colors = CardDefaults.cardColors(containerColor = MotorSurface), shape = RoundedCornerShape(17.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)) {
        Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) { Text(icon, style = MaterialTheme.typography.titleLarge); Text(title, fontWeight = FontWeight.Bold, color = MotorText) }
    }
}

@Composable
private fun HomeEventCard(event: Event, onClick: () -> Unit) {
    val date = remember(event.date, event.hour) {
        val d = SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("es-UY")).format(Date(event.date)).replaceFirstChar { it.uppercase() }
        if (event.hour.isNotBlank()) "$d · ${event.hour}" else d
    }
    Card(onClick = onClick, modifier = Modifier.width(280.dp), colors = CardDefaults.cardColors(containerColor = MotorSurface), shape = RoundedCornerShape(19.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MotorLine)) {
        Column {
            Box(Modifier.fillMaxWidth().height(118.dp).background(Brush.linearGradient(listOf(Color(0xFF252B36), Color(0xFF14161B))))) {
                Text("🏎️", fontSize = 58.sp, modifier = Modifier.align(Alignment.Center))
                Surface(Modifier.padding(10.dp).align(Alignment.TopEnd), color = Color.Black.copy(alpha = .55f), shape = RoundedCornerShape(9.dp)) { Text(date, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = Color.White, style = MaterialTheme.typography.labelSmall) }
            }
            Column(Modifier.padding(14.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text("📍 ${event.city}, ${event.location}", color = MotorMuted, maxLines = 1, modifier = Modifier.padding(top = 6.dp))
                Text("🔥 Próxima juntada", color = MotorRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    currentUserId: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLike: () -> Unit,
    isLiked: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.userPhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = post.userPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(post.userName, fontWeight = FontWeight.Bold)
                    if (post.isUserVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(compact = true)
                    }
                }
                
                if (post.userId == currentUserId) {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("¿Eliminar publicación?") },
                            text = { Text("Esta acción no se puede deshacer.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    onDelete()
                                    showDeleteDialog = false
                                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content)
            
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_report_image)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Me gusta",
                        tint = if (isLiked) MotorRed else MotorMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likesCount} Me gusta",
                    style = MaterialTheme.typography.bodySmall,
                    color = MotorMuted
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, modifier = Modifier.size(16.dp), tint = MotorMuted)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.commentsCount} comentarios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MotorMuted,
                )
            }
        }
    }
}
