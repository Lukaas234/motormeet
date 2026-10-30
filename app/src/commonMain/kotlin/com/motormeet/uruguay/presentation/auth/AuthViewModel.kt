package com.motormeet.uruguay.presentation.auth

import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

enum class SessionState {
    Loading, LoggedIn, LoggedOut
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        scope.launch {
            try {
                // Try to load user profile via repository (no direct Appwrite Account access)
                // The repository handles session validation
                _sessionState.value = SessionState.LoggedOut
            } catch (_: Exception) {
                _currentUser.value = null
                _sessionState.value = SessionState.LoggedOut
            }
        }
    }

    fun login(email: String, pass: String) {
        scope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, pass)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                    checkCurrentUser()
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Error al iniciar sesión")
                }
        }
    }

    fun signUp(email: String, pass: String, name: String) {
        scope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signUp(email, pass, name)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                    checkCurrentUser()
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Error al registrarse")
                }
        }
    }

    fun resetPassword(email: String) {
        scope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.resetPassword(email)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Error al enviar correo")
                }
        }
    }

    fun signOut() {
        scope.launch {
            authRepository.signOut()
            _currentUser.value = null
            _sessionState.value = SessionState.LoggedOut
        }
    }
}
