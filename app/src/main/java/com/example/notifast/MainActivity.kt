package com.example.notifast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        
        // Configuración para ocultar la interfaz del sistema (Status Bar y Navigation Bar)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
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

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            alwaysShowLabel = true, // Asegura que el espacio esté reservado
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
                                        modifier = Modifier.size(24.dp) // Reducido ligeramente de 26 a 24 para ganar espacio vertical
                                    ) 
                                } 
                            },
                            label = { 
                                Text(
                                    text = screen.title ?: "", 
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp, // Tamaño reducido para evitar el salto de línea
                                        letterSpacing = (-0.3).sp // Espaciado entre letras negativo para compactar
                                    ),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Visible
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
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(backgroundGradient)
        ) {
            NavGraph(
                navController = navController,
                authViewModel = authViewModel,
                notesViewModel = notesViewModel
            )
        }
    }
}
