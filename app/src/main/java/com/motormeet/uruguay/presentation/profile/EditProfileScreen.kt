package com.motormeet.uruguay.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.motormeet.uruguay.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User? = null,
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel
) {
    val currentUser by viewModel.user.collectAsState()
    val userToEdit = user ?: currentUser

    var displayName by remember(userToEdit) { mutableStateOf(userToEdit?.displayName ?: "") }
    var bio by remember(userToEdit) { mutableStateOf(userToEdit?.bio ?: "") }
    var city by remember(userToEdit) { mutableStateOf(userToEdit?.city ?: "") }
    var instagram by remember(userToEdit) { mutableStateOf(userToEdit?.socialLinks?.get("instagram") ?: "") }
    var twitter by remember(userToEdit) { mutableStateOf(userToEdit?.socialLinks?.get("twitter") ?: "") }
    var web by remember(userToEdit) { mutableStateOf(userToEdit?.socialLinks?.get("web") ?: "") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(userToEdit) {
        userToEdit?.let {
            displayName = it.displayName
            bio = it.bio
            city = it.city
            instagram = it.socialLinks["instagram"] ?: ""
            twitter = it.socialLinks["twitter"] ?: ""
            web = it.socialLinks["web"] ?: ""
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is EditProfileUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Nueva foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!userToEdit?.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = userToEdit?.photoUrl,
                        contentDescription = "Foto actual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.padding(4.dp).size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Información Básica", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Nombre de usuari") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Ciudad o departamento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Biografía") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Redes Sociales", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = instagram,
                onValueChange = { instagram = it },
                label = { Text("Instagram (@usuario)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = twitter,
                onValueChange = { twitter = it },
                label = { Text("Twitter/X (@usuario)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = web,
                onValueChange = { web = it },
                label = { Text("Sitio Web") },
                modifier = Modifier.fillMaxWidth()
            )

            if (userToEdit != null) {
                HorizontalDivider()
                Text("Estadísticas (Lectura)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatReadOnlyItem("Seguidores", userToEdit.followerCount)
                    StatReadOnlyItem("Seguidos", userToEdit.followingCount)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatReadOnlyItem("Eventos Creados", userToEdit.createdEventsCount)
                    StatReadOnlyItem("Eventos Asistidos", userToEdit.attendedEventsCount)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    viewModel.updateProfile(
                        displayName = displayName,
                        bio = bio,
                        city = city,
                        instagram = instagram,
                        twitter = twitter,
                        web = web,
                        newPhotoUri = selectedImageUri?.toString()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is EditProfileUiState.Loading && displayName.isNotBlank()
            ) {
                if (uiState is EditProfileUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar cambios")
                }
            }
            
            if (uiState is EditProfileUiState.Error) {
                Text(
                    text = (uiState as EditProfileUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatReadOnlyItem(label: String, count: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(count.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
