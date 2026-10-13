package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StudioTopHeader
import com.example.ui.components.HairStudioBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppDestination
import com.example.viewmodel.HairStudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: HairStudioViewModel = viewModel()
                HairStudioApp(viewModel)
            }
        }
    }
}

@Composable
fun HairStudioApp(viewModel: HairStudioViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button when on sub-screens
    if (currentScreen != AppDestination.HOME) {
        BackHandler {
            when (currentScreen) {
                AppDestination.RESULTS -> viewModel.navigateTo(AppDestination.STUDIO)
                AppDestination.PRIVACY -> viewModel.navigateTo(AppDestination.DASHBOARD)
                else -> viewModel.navigateTo(AppDestination.HOME)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != AppDestination.RESULTS) {
                StudioTopHeader(
                    currentScreen = currentScreen,
                    onNavigate = { destination ->
                        viewModel.navigateTo(destination)
                    }
                )
            }
        },
        bottomBar = {
            HairStudioBottomBar(
                currentScreen = currentScreen,
                onNavigate = { destination ->
                    viewModel.navigateTo(destination)
                }
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            AppDestination.HOME -> HomeScreen(viewModel, screenModifier)
            AppDestination.GALLERY -> GalleryScreen(viewModel, screenModifier)
            AppDestination.STUDIO -> StudioScreen(viewModel, screenModifier)
            AppDestination.RESULTS -> ResultsScreen(viewModel, screenModifier)
            AppDestination.RECOMMENDATIONS -> RecommendationsScreen(viewModel, screenModifier)
            AppDestination.DASHBOARD -> DashboardScreen(viewModel, screenModifier)
            AppDestination.PRIVACY -> PrivacyPolicyScreen(viewModel, screenModifier)
        }
    }
}
