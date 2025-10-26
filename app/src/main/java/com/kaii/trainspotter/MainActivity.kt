package com.kaii.trainspotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.api.LocationShortCodeMap
import com.kaii.trainspotter.api.Stop
import com.kaii.trainspotter.api.StopGroup
import com.kaii.trainspotter.compose.screens.LoginScreen
import com.kaii.trainspotter.compose.screens.SearchScreen
import com.kaii.trainspotter.compose.screens.Settings
import com.kaii.trainspotter.compose.screens.TimeTableScreen
import com.kaii.trainspotter.compose.screens.TrainDetailsScreen
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.models.main.MainViewModel
import com.kaii.trainspotter.models.main.MainViewModelFactory
import com.kaii.trainspotter.ui.theme.TrainSpotterTheme
import kotlin.reflect.typeOf

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
            TrainSpotterTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(applicationContext)
                )

                val apiKey by mainViewModel.settings.user.getApiKey().collectAsState(initial = ApiKey.NotAvailable)
                val savedApiKey = rememberSaveable(
                    saver = ApiKey.Saver,
                    inputs = arrayOf(apiKey)
                ) {
                    apiKey
                }

                // preload short code map for performance reasons (unknown if significant)
                LocationShortCodeMap.preloadMap(context = applicationContext)

                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalMainViewModel provides mainViewModel
                ) {
                    Content(apiKey = savedApiKey)
                }
            }
        }
    }

    @Composable
    fun Content(apiKey: ApiKey) {
        val navController = LocalNavController.current

        NavHost(
            navController = navController,
            startDestination =
                if (apiKey is ApiKey.NotAvailable) Screens.Login
                else Screens.Search,
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
            composable<Screens.Login> {
                LoginScreen()
            }

            composable<Screens.TimeTable>(
                typeMap = mapOf(
                    typeOf<StopGroup>() to StopGroup.StopGroupNavType,
                    typeOf<Stop>() to Stop.StopNavType,
                    typeOf<Alert>() to Alert.AlertNavType
                )
            ) {
                val screen = it.toRoute<Screens.TimeTable>()

                TimeTableScreen(
                    apiKey = (apiKey as ApiKey.Available).realtimeKey,
                    stopId = screen.stopId,
                    stopName = screen.stopName
                )
            }

            composable<Screens.Search> {
                SearchScreen(
                    apiKey = (apiKey as ApiKey.Available)
                )
            }

            composable<Screens.Settings> {
                Settings()
            }

            composable<Screens.TrainDetails> {
                val screen = it.toRoute<Screens.TrainDetails>()
                TrainDetailsScreen(
                    apiKey = (apiKey as ApiKey.Available).trafikVerketKey,
                    trainId = screen.trainId
                )
            }
        }
    }
}