package com.kaii.trainspotter.compose.screens

import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trainspotter.LocalMainViewModel
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.ApiKeyPreferenceRow
import com.kaii.trainspotter.compose.widgets.PreferencesSeparatorText
import com.kaii.trainspotter.compose.widgets.TextPreferencesRow
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.TextStylingConstants

@Composable
fun Settings(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar()
        },
        modifier = modifier
            .padding(8.dp)
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current

        LazyColumn(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    start = innerPadding.calculateStartPadding(layoutDirection) + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 8.dp,
                    end = innerPadding.calculateEndPadding(layoutDirection) + 8.dp
                )
                .fillMaxWidth()
        ) {
            item {
                PreferencesSeparatorText(
                    text = stringResource(id = R.string.login)
                )
            }
            item {
                val mainViewModel = LocalMainViewModel.current
                val apiKey by mainViewModel.settings.user.getApiKey().collectAsStateWithLifecycle(initialValue = ApiKey.NotAvailable)

                ApiKeyPreferenceRow(
                    initialKey = apiKey,
                    isRealtimeKey = true,
                    setKey = { new ->
                        if (new.isBlank()) {
                            mainViewModel.settings.user.setApiKey(ApiKey.NotAvailable)
                        } else {
                            mainViewModel.settings.user.setApiKey(
                                if (apiKey is ApiKey.Available) {
                                    (apiKey as ApiKey.Available).copy(
                                        realtimeKey = new
                                    )
                                } else {
                                    ApiKey.Available(
                                        realtimeKey = new,
                                        trafikVerketKey = ""
                                    )
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ApiKeyPreferenceRow(
                    initialKey = apiKey,
                    isRealtimeKey = false,
                    setKey = { new ->
                        if (new.isBlank()) {
                            mainViewModel.settings.user.setApiKey(ApiKey.NotAvailable)
                        } else {
                            mainViewModel.settings.user.setApiKey(
                                if (apiKey is ApiKey.Available) {
                                    (apiKey as ApiKey.Available).copy(
                                        trafikVerketKey = new
                                    )
                                } else {
                                    ApiKey.Available(
                                        realtimeKey = "",
                                        trafikVerketKey = new
                                    )
                                }
                            )
                        }
                    }
                )
            }

            item {
                PreferencesSeparatorText(
                    text = stringResource(id = R.string.settings_about)
                )
            }

            item {
                val context = LocalContext.current
                val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"

                TextPreferencesRow(
                    title = stringResource(id = R.string.settings_developer),
                    text = "kaii-lb",
                    icon = R.drawable.code,
                ) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "https://github.com/kaii-lb".toUri()
                    }

                    context.startActivity(intent)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextPreferencesRow(
                    title = stringResource(id = R.string.settings_version),
                    text = version,
                    icon = R.drawable.info,
                ) {}
            }

            item {
                PreferencesSeparatorText(
                    text = stringResource(id = R.string.debugging)
                )
            }

            item {
                val navController = LocalNavController.current
                TextPreferencesRow(
                    title = stringResource(id = R.string.service_testing),
                    text = stringResource(id = R.string.service_testing_desc),
                    icon = R.drawable.bug_report,
                ) {
                    navController.navigate(Screens.ServiceTesting)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Return to previous page"
                )
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.settings),
                fontSize = TextStylingConstants.SIZE_LARGE
            )
        },
        modifier = modifier
    )
}