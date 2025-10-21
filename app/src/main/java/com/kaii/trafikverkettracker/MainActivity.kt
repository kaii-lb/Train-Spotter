package com.kaii.trafikverkettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kaii.trafikverkettracker.compose.screens.LoginScreen
import com.kaii.trafikverkettracker.compose.screens.TimeTableScreen
import com.kaii.trafikverkettracker.helpers.Screens
import com.kaii.trafikverkettracker.models.main.MainViewModel
import com.kaii.trafikverkettracker.models.main.MainViewModelFactory
import com.kaii.trafikverkettracker.ui.theme.TrafikVerketTrackerTheme

val LocalNavController = compositionLocalOf<NavHostController> {
    throw IllegalStateException("CompositionLocal LocalNavController not present")
}
val LocalMainViewModel = compositionLocalOf<MainViewModel> {
    throw IllegalStateException("CompositionLocal LocalMainViewModel not present")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrafikVerketTrackerTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(applicationContext)
                )

                val apiKey by mainViewModel.settings.user.getApiKey().collectAsState(initial = "-1")
                if (apiKey != "-1") { // TODO: please use something else-
                    CompositionLocalProvider(
                        LocalNavController provides navController,
                        LocalMainViewModel provides mainViewModel
                    ) {
                        Content(apiKey = apiKey)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }

    @Composable
    fun Content(apiKey: String?) {
        val navController = LocalNavController.current

        NavHost(
            navController = navController,
            startDestination =
                if (apiKey == null) Screens.LoginScreen
                else Screens.MainScreen(apiKey = apiKey),
            modifier = Modifier
                .fillMaxSize(1f)
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                slideInHorizontally { width -> width } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally { width -> -width } + fadeOut()
            },
            popExitTransition = {
                slideOutHorizontally { width -> width } + fadeOut()
            },
            popEnterTransition = {
                slideInHorizontally { width -> -width } + fadeIn()
            }
        ) {
            composable<Screens.LoginScreen> {
                LoginScreen()
            }

            composable<Screens.MainScreen> {
                val screen = it.toRoute<Screens.MainScreen>()

                TimeTableScreen(apiKey = screen.apiKey)
            }
        }
    }
}