package com.example.notifast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notifast.navigation.NavGraph
import com.example.notifast.navigation.Screen
import com.example.notifast.ui.theme.NotiFastTheme
import com.example.notifast.ui.theme.Ruby
import com.example.notifast.viewmodel.AuthViewModel
import com.example.notifast.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiFastTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val notesViewModel: NotesViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = Screen.bottomNavItems.any { it.route == currentDestination?.route }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color.White, Ruby.copy(alpha = 0.15f))
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundGradient)
    ) {
        NavGraph(
            navController = navController,
            authViewModel = authViewModel,
            notesViewModel = notesViewModel
        )

        // HOTBAR FLOTANTE CENTRADA Y SEGURA
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Respeta la zona de botones del sistema
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp) // Margen de cápsula flotante
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.98f),
                    shadowElevation = 12.dp,
                    tonalElevation = 4.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        // WindowInsets(0) es la clave para centrar verticalmente y evitar cortes
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.height(84.dp),
                        tonalElevation = 0.dp
                    ) {
                        Screen.bottomNavItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { 
                                    screen.icon?.let { 
                                        Icon(
                                            imageVector = it, 
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(26.dp)
                                        ) 
                                    } 
                                },
                                label = { 
                                    Text(
                                        text = screen.title ?: "", 
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Ruby,
                                    selectedTextColor = Ruby,
                                    indicatorColor = Ruby.copy(alpha = 0.1f),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
