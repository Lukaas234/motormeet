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
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User?,
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel
) {
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var city by remember { mutableStateOf(user?.city ?: "") }
    var instagram by remember { mutableStateOf("") }
    var twitter by remember { mutableStateOf("") }
    var web by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = "Editar perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Biografía") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            singleLine = false,
            maxLines = 3
        )
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Ciudad") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = instagram,
            onValueChange = { instagram = it },
            label = { Text("Instagram") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = twitter,
            onValueChange = { twitter = it },
            label = { Text("Twitter") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = web,
            onValueChange = { web = it },
            label = { Text("Sitio web") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.updateProfile(
                    displayName = displayName,
                    bio = bio,
                    city = city,
                    instagram = instagram,
                    twitter = twitter,
                    web = web
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is EditProfileUiState.Loading
        ) {
            if (uiState is EditProfileUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar cambios", fontWeight = FontWeight.ExtraBold)
            }
        }

        if (uiState is EditProfileUiState.Error) {
            Text(
                text = (uiState as EditProfileUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
