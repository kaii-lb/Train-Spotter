package com.kaii.trainspotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kaii.lavender.snackbars.LavenderSnackbarBox
import com.kaii.lavender.snackbars.LavenderSnackbarHostState
import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.api.LocationShortCodeMap
import com.kaii.trainspotter.api.RailwayEventCodeMap
import com.kaii.trainspotter.api.Stop
import com.kaii.trainspotter.api.StopGroup
import com.kaii.trainspotter.compose.screens.LoginScreen
import com.kaii.trainspotter.compose.screens.SearchScreen
import com.kaii.trainspotter.compose.screens.ServiceTesting
import com.kaii.trainspotter.compose.screens.Settings
import com.kaii.trainspotter.compose.screens.TimeTableScreen
import com.kaii.trainspotter.compose.screens.TrainDetailsScreen
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.models.SettingsViewModel
import com.kaii.trainspotter.ui.theme.TrainSpotterTheme
import dagger.hilt.android.AndroidEntryPoint
import org.maplibre.android.MapLibre
import kotlin.reflect.typeOf

val LocalNavController = compositionLocalOf<NavHostController> {
    throw IllegalStateException("CompositionLocal LocalNavController not present")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        MapLibre.getInstance(applicationContext)

        enableEdgeToEdge()

        val mainViewModel by viewModels<SettingsViewModel>()

        setContent {
            TrainSpotterTheme {
                val navController = rememberNavController()

                val apiKey by mainViewModel.apiKey.collectAsStateWithLifecycle()
                val savedApiKey = rememberSaveable(
                    saver = ApiKey.Saver,
                    inputs = arrayOf(apiKey)
                ) { apiKey }

                // preload short code map for performance reasons (unknown if significant)
                LocationShortCodeMap.preloadMap(context = applicationContext)

                // preload
                RailwayEventCodeMap.preloadMap(context = applicationContext)

                CompositionLocalProvider(
                    LocalNavController provides navController
                ) {
                    val snackbarHostState = remember {
                        LavenderSnackbarHostState()
                    }

                    LavenderSnackbarBox(snackbarHostState = snackbarHostState) {
                        Content(apiKey = savedApiKey)
                    }
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
                LoginScreen(viewModel = hiltViewModel())
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
                    stopId = screen.stopId,
                    stopName = screen.stopName,
                    viewModel = hiltViewModel()
                )
            }

            composable<Screens.Search> {
                SearchScreen(viewModel = hiltViewModel())
            }

            composable<Screens.Settings> {
                Settings(viewModel = hiltViewModel())
            }

            composable<Screens.TrainDetails> {
                val screen = it.toRoute<Screens.TrainDetails>()

                TrainDetailsScreen(
                    trainId = screen.trainId,
                    viewModel = hiltViewModel()
                )
            }

            composable<Screens.ServiceTesting> {
                ServiceTesting(
                    apiKey = { apiKey }
                )
            }
        }
    }
}