package com.motormeet.uruguay.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.motormeet.uruguay.presentation.auth.AuthViewModel
import com.motormeet.uruguay.presentation.auth.ForgotPasswordScreen
import com.motormeet.uruguay.presentation.auth.LoginScreen
import com.motormeet.uruguay.presentation.auth.SignUpScreen
import com.motormeet.uruguay.presentation.auth.SessionState
import com.motormeet.uruguay.presentation.channels.*
import com.motormeet.uruguay.presentation.events.*
import com.motormeet.uruguay.presentation.garage.*
import com.motormeet.uruguay.presentation.home.*
import com.motormeet.uruguay.presentation.profile.*
import com.motormeet.uruguay.ui.theme.MotorBlack
import com.motormeet.uruguay.ui.theme.MotorRed

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Inicio", Icons.Default.Home)
    object Map : BottomNavItem("events_map", "Mapa", Icons.Default.Map)
    object Events : BottomNavItem("events", "Eventos", Icons.Default.CalendarMonth)
    object Channels : BottomNavItem("channels", "Canales", Icons.Default.Campaign)
    object Garage : BottomNavItem("garage", "Garaje", Icons.Default.DirectionsCar)
    object Profile : BottomNavItem("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val sessionState by authViewModel.sessionState.collectAsState()

    when (sessionState) {
        SessionState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MotorBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotorRed)
            }
        }
        SessionState.LoggedOut -> {
            val authNavController = rememberNavController()
            NavHost(
                navController = authNavController,
                startDestination = "login"
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { authViewModel.checkCurrentUser() },
                        onNavigateToSignUp = { authNavController.navigate("signup") },
                        onNavigateToForgotPassword = { authNavController.navigate("forgot_password") },
                        viewModel = hiltViewModel()
                    )
                }
                composable("signup") {
                    SignUpScreen(
                        onSignUpSuccess = { 
                            authViewModel.checkCurrentUser()
                            authNavController.popBackStack() 
                        },
                        onNavigateBack = { authNavController.popBackStack() },
                        viewModel = hiltViewModel()
                    )
                }
                composable("forgot_password") {
                    ForgotPasswordScreen(
                        onNavigateBack = { authNavController.popBackStack() },
                        viewModel = hiltViewModel()
                    )
                }
            }
        }
        SessionState.LoggedIn -> {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val bottomNavItems = listOf(
                BottomNavItem.Home,
                BottomNavItem.Map,
                BottomNavItem.Events,
                BottomNavItem.Channels,
                BottomNavItem.Garage,
                BottomNavItem.Profile
            )

            val showBottomBar = bottomNavItems.any { it.route == currentRoute }

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MotorRed,
                                        selectedTextColor = MotorRed,
                                        indicatorColor = MotorRed.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToGarage = { navController.navigate("garage") },
                            onNavigateToProfile = { navController.navigate("profile") },
                            onNavigateToCreatePost = { navController.navigate("create_post") },
                            onNavigateToUserProfile = { userId -> navController.navigate("user_profile/$userId") },
                            onNavigateToPostDetail = { postId -> navController.navigate("post_detail/$postId") },
                            onNavigateToEvents = { navController.navigate("events") },
                            viewModel = hiltViewModel(),
                            eventViewModel = hiltViewModel()
                        )
                    }
                    composable("create_post") {
                        CreatePostScreen(
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("post_detail/{postId}") { backStackEntry ->
                        val postId = backStackEntry.arguments?.getString("postId") ?: ""
                        PostDetailScreen(
                            postId = postId,
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("events_map") {
                        EventsMapScreen(
                            onNavigateToEventDetail = { eventId -> navController.navigate("event_detail/$eventId") },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("events") {
                        EventListScreen(
                            onNavigateToCreateEvent = { navController.navigate("create_event") },
                            onNavigateToEventDetail = { eventId -> navController.navigate("event_detail/$eventId") },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("create_event") {
                        CreateEventScreen(
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("event_detail/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        EventDetailScreen(
                            eventId = eventId,
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("channels") {
                        ChannelsScreen(
                            onNavigateToCreateChannel = { navController.navigate("create_channel") },
                            onNavigateToChannelDetail = { channelId -> navController.navigate("channel_detail/$channelId") },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("create_channel") {
                        CreateChannelScreen(
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("channel_detail/{channelId}") { backStackEntry ->
                        val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                        ChannelDetailScreen(
                            channelId = channelId,
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("garage") {
                        GarageScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onAddVehicle = { navController.navigate("add_vehicle") },
                            onNavigateToDetail = { vehicleId -> navController.navigate("vehicle_detail/$vehicleId") },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("add_vehicle") {
                        AddVehicleScreen(
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("vehicle_detail/{vehicleId}") { backStackEntry ->
                        val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
                        VehicleDetailScreen(
                            vehicleId = vehicleId,
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            userId = null,
                            onSignOut = { authViewModel.signOut() },
                            onNavigateToEditProfile = { navController.navigate("edit_profile") },
                            onNavigateToPostDetail = { postId -> navController.navigate("post_detail/$postId") },
                            viewModel = hiltViewModel(),
                            authViewModel = authViewModel
                        )
                    }
                    composable("user_profile/{userId}") { backStackEntry ->
                        val targetUserId = backStackEntry.arguments?.getString("userId")
                        ProfileScreen(
                            userId = targetUserId,
                            onSignOut = {},
                            onNavigateToEditProfile = {},
                            onNavigateToPostDetail = { postId -> navController.navigate("post_detail/$postId") },
                            viewModel = hiltViewModel(),
                            authViewModel = authViewModel
                        )
                    }
                    composable("edit_profile") {
                        EditProfileScreen(
                            user = null,
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = hiltViewModel()
                        )
                    }
                }
            }
        }
    }
}
