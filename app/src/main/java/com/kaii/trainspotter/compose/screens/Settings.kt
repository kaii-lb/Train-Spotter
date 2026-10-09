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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.ApiKeyPreferenceRow
import com.kaii.trainspotter.compose.widgets.PreferencesSeparatorText
import com.kaii.trainspotter.compose.widgets.TextPreferencesRow
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.models.SettingsViewModel
import com.kaii.trainspotter.presentation.RowPosition

@Composable
fun Settings(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()

    Settings(
        apiKey = { apiKey },
        modifier = modifier,
        onSetApiKey = viewModel::setApiKey
    )
}

@Composable
fun Settings(
    apiKey: () -> ApiKey,
    modifier: Modifier = Modifier,
    onSetApiKey: (newKey: ApiKey) -> Unit
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
                ApiKeyPreferenceRow(
                    initialKey = apiKey(),
                    isRealtimeKey = true,
                    position = RowPosition.Top,
                    setKey = { new ->
                        if (new.isBlank()) {
                            onSetApiKey(ApiKey.NotAvailable)
                        } else {
                            onSetApiKey(
                                if (apiKey() is ApiKey.Available) {
                                    (apiKey() as ApiKey.Available).copy(
                                        realtimeKey = new
                                    )
                                } else {
                                    ApiKey.Available(
                                        realtimeKey = new,
                                        trafikverketKey = ""
                                    )
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ApiKeyPreferenceRow(
                    initialKey = apiKey(),
                    isRealtimeKey = false,
                    position = RowPosition.Bottom,
                    setKey = { new ->
                        if (new.isBlank()) {
                            onSetApiKey(ApiKey.NotAvailable)
                        } else {
                            val newKey = if (apiKey() is ApiKey.Available) {
                                (apiKey() as ApiKey.Available).copy(
                                    trafikverketKey = new
                                )
                            } else {
                                ApiKey.Available(
                                    realtimeKey = "",
                                    trafikverketKey = new
                                )
                            }

                            onSetApiKey(newKey)
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
                val version = remember {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
                }

                TextPreferencesRow(
                    title = stringResource(id = R.string.settings_developer),
                    text = "kaii-lb",
                    icon = R.drawable.code,
                    position = RowPosition.Top
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
                    position = RowPosition.Bottom,
                    onClick = {}
                )
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