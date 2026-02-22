package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.Screen
import com.example.myapplication.screens.EnvironmentMonitorScreen
import com.example.myapplication.screens.MedicationHistoryScreen
import com.example.myapplication.screens.MedicineInfoScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.MedicineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: MedicineViewModel = viewModel()
                
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.MedicineInfo.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.MedicineInfo.route) {
                            MedicineInfoScreen(viewModel = viewModel)
                        }
                        composable(Screen.MedicationHistory.route) {
                            MedicationHistoryScreen(viewModel = viewModel)
                        }
                        composable(Screen.EnvironmentMonitor.route) {
                            EnvironmentMonitorScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.MedicineInfo.route, "药品信息", Icons.Default.Home),
        BottomNavItem(Screen.MedicationHistory.route, "服药记录", Icons.Default.DateRange),
        BottomNavItem(Screen.EnvironmentMonitor.route, "环境监控", Icons.Default.Settings)
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)