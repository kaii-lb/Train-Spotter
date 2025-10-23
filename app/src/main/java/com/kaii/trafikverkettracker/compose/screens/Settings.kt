package com.kaii.trafikverkettracker.compose.screens

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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trafikverkettracker.LocalMainViewModel
import com.kaii.trafikverkettracker.LocalNavController
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.compose.widgets.ApiKeyPreferenceRow
import com.kaii.trafikverkettracker.compose.widgets.PreferencesSeparatorText
import com.kaii.trafikverkettracker.datastore.ApiKey
import com.kaii.trafikverkettracker.helpers.TextStylingConstants

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
                text = stringResource(id = R.string.search),
                fontSize = TextStylingConstants.SIZE_LARGE
            )
        },
        modifier = modifier
    )
}