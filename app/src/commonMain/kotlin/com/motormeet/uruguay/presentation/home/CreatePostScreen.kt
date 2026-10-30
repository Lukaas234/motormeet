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
import com.motormeet.uruguay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreatePostViewModel
) {
    var content by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = "Nueva publicación",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            )
        }

        Text(
            text = "¿Qué estás compartiendo hoy?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start).padding(top = 24.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Escribe una publicación...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.createPost(content) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is CreatePostUiState.Loading && content.isNotBlank()
        ) {
            if (uiState is CreatePostUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Publicar", fontWeight = FontWeight.ExtraBold)
            }
        }

        if (uiState is CreatePostUiState.Error) {
            Text(
                text = (uiState as CreatePostUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
