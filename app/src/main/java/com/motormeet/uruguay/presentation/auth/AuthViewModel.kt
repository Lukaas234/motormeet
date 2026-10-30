package com.motormeet.uruguay.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import io.appwrite.services.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SessionState {
    Loading, LoggedIn, LoggedOut
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

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
        viewModelScope.launch {
            try {
                val appwriteUser = account.get()
                userRepository.getUserProfile(appwriteUser.id)
                    .onEach { profile ->
                        _currentUser.value = profile ?: User(id = appwriteUser.id, email = appwriteUser.email, displayName = appwriteUser.name)
                        _sessionState.value = SessionState.LoggedIn
                    }
                    .launchIn(viewModelScope)
            } catch (_: Exception) {
                _currentUser.value = null
                _sessionState.value = SessionState.LoggedOut
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.value = null
            _sessionState.value = SessionState.LoggedOut
        }
    }
}
