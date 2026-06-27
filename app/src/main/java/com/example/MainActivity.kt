package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.form.FormScreen
import com.example.ui.screens.groups.GroupsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.messages.MessagesScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase and start real-time data syncing if credentials are provided
        com.example.data.repository.FirebaseRepository.initialize(applicationContext)
        com.example.data.repository.MockRepository.startSyncing()

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val isConnectionError by viewModel.isConnectionError.collectAsState()
    var isRetryingConnection by remember { mutableStateOf(false) }

    // Observe current destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Listen to currentUser state changes for redirection
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            // Not logged in -> go to login
            if (currentRoute != "login" && currentRoute != "register") {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // Logged in -> go to home (explore) if on login/register
            if (currentRoute == null || currentRoute == "login" || currentRoute == "register") {
                navController.navigate("explore") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // 1. Connection Error Overlay
        if (isConnectionError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Wifi Off",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Bağlantı Hatası",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sistem bağlantısı kurulamadı. Lütfen internet bağlantınızı kontrol edin veya demo modunda devam edin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        if (isRetryingConnection) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.triggerConnectionError(false)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Demo Moduna Devam Et", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        isRetryingConnection = true
                                        // Simulate a quick network retry sequence
                                        viewModel.triggerConnectionError(false)
                                        isRetryingConnection = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tekrar Dene", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 3. Authenticated App Layout (Main bottom navigation NavHost shell)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    // Only show bottom navigation if logged in and not on login/register screens
                    if (currentUser != null && currentRoute != "login" && currentRoute != "register") {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            // Explore Tab
                            NavigationBarItem(
                                selected = currentRoute == "explore",
                                onClick = {
                                    if (currentRoute != "explore") {
                                        navController.navigate("explore") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Explore, contentDescription = "Keşfet") },
                                label = { Text("Keşfet", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Groups Tab
                            NavigationBarItem(
                                selected = currentRoute == "groups",
                                onClick = {
                                    if (currentRoute != "groups") {
                                        navController.navigate("groups") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "Gruplar") },
                                label = { Text("Gruplar", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Messages Tab
                            NavigationBarItem(
                                selected = currentRoute == "messages",
                                onClick = {
                                    if (currentRoute != "messages") {
                                        navController.navigate("messages") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Forum, contentDescription = "Mesajlar") },
                                label = { Text("Mesajlar", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Panel Tab
                            NavigationBarItem(
                                selected = currentRoute == "panel",
                                onClick = {
                                    if (currentRoute != "panel") {
                                        navController.navigate("panel") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Panel") },
                                label = { Text("Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )

                            // Profile Tab
                            NavigationBarItem(
                                selected = currentRoute == "profile",
                                onClick = {
                                    if (currentRoute != "profile") {
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profil") },
                                label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "login" else "explore",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Login Screen
                    composable("login") {
                        AuthScreen(
                            viewModel = viewModel,
                            initialIsLoginMode = true,
                            onAuthSuccess = {
                                navController.navigate("explore") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    // Register Screen
                    composable("register") {
                        AuthScreen(
                            viewModel = viewModel,
                            initialIsLoginMode = false,
                            onAuthSuccess = {
                                navController.navigate("explore") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                navController.navigate("login")
                            }
                        )
                    }

                    // Explore Screen
                    composable("explore") {
                        ExploreScreen(
                            viewModel = viewModel,
                            onAddRequested = {
                                navController.navigate("add_event")
                            },
                            onEventInspect = {
                                // Dynamic event inspection can route to project adding for testing
                                navController.navigate("add_project")
                            }
                        )
                    }

                    // Add Event Form Screen
                    composable("add_event") {
                        FormScreen(
                            viewModel = viewModel,
                            initialTab = 0,
                            onBackRequested = {
                                navController.navigateUp()
                            }
                        )
                    }

                    // Add Project Form Screen
                    composable("add_project") {
                        FormScreen(
                            viewModel = viewModel,
                            initialTab = 1,
                            onBackRequested = {
                                navController.navigateUp()
                            }
                        )
                    }

                    // Groups Screen
                    composable("groups") {
                        GroupsScreen(
                            viewModel = viewModel
                        )
                    }

                    // Messages Screen
                    composable("messages") {
                        MessagesScreen(
                            viewModel = viewModel
                        )
                    }

                    // Panel (Dashboard) Screen
                    composable("panel") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToEvents = {
                                navController.navigate("explore")
                            },
                            onNavigateToProjects = {
                                navController.navigate("explore")
                            }
                        )
                    }

                    // Profile Screen
                    composable("profile") {
                        ProfileScreen(
                            viewModel = viewModel,
                            onLogoutRequested = {
                                viewModel.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}
