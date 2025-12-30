package com.kaii.trainspotter.compose.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaii.lavender.snackbars.LavenderSnackbarController
import com.kaii.lavender.snackbars.LavenderSnackbarEvents
import com.kaii.trainspotter.LocalMainViewModel
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.RealtimeClient
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.compose.widgets.ApiKeyExplanationDialog
import com.kaii.trainspotter.compose.widgets.NotificationPermissionDialog
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember {
        mutableStateOf(
            context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED
        )
    }
    if (showDialog) {
        NotificationPermissionDialog {
            showDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopBar()
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize(1f)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var realtimeKey by rememberSaveable { mutableStateOf("") }
            var trafikVerketKey by rememberSaveable { mutableStateOf("") }

            Text(
                text = stringResource(id = R.string.login),
                fontSize = TextStylingConstants.SIZE_LARGE,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = realtimeKey,
                onValueChange = { new ->
                    realtimeKey = new
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_api_key_realtime)
                    )
                },
                prefix = {
                    Icon(
                        painter = painterResource(id = R.drawable.key),
                        contentDescription = stringResource(id = R.string.login_api_key_realtime),
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                },
                singleLine = true,
                isError = realtimeKey.isBlank(),
                shape = RoundedCornerShape(RoundedCornerConstants.ROUNDING_MEDIUM),
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )

            TextField(
                value = trafikVerketKey,
                onValueChange = { new ->
                    trafikVerketKey = new
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_api_key_trafikverket)
                    )
                },
                prefix = {
                    Icon(
                        painter = painterResource(id = R.drawable.key),
                        contentDescription = stringResource(id = R.string.login_api_key_trafikverket),
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                },
                singleLine = true,
                isError = trafikVerketKey.isBlank(),
                shape = RoundedCornerShape(RoundedCornerConstants.ROUNDING_MEDIUM),
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )

            var showGetApiKeyDialog by remember { mutableStateOf(false) }
            if (showGetApiKeyDialog) {
                ApiKeyExplanationDialog {
                    showGetApiKeyDialog = false
                }
            }

            Text(
                text = stringResource(id = R.string.login_api_key_missing),
                style = TextStyle(
                    fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                    textAlign = TextAlign.Start,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clickable {
                        showGetApiKeyDialog = true
                    }
            )

            val mainViewModel = LocalMainViewModel.current
            val context = LocalContext.current
            val resources = LocalResources.current
            val coroutineScope = rememberCoroutineScope()

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val realtimeClient =
                            RealtimeClient(
                                context = context,
                                apiKey = realtimeKey
                            )
                        val trafikVerketClient =
                            TrafikverketClient(
                                context = context,
                                apiKey = trafikVerketKey
                            )

                        val response1 = realtimeClient.findStopGroups(name = "malmö")
                        val response2 = trafikVerketClient.getRouteDataForId(trainId = "1778")

                        if (response1 == null) {
                            LavenderSnackbarController.pushEvent(
                                LavenderSnackbarEvents.MessageEvent(
                                    message = resources.getString(R.string.login_realtime_key_wrong),
                                    icon = R.drawable.key,
                                    duration = SnackbarDuration.Short
                                )
                            )
                        } else if (response2.isEmpty()) {
                            LavenderSnackbarController.pushEvent(
                                LavenderSnackbarEvents.MessageEvent(
                                    message = resources.getString(R.string.login_trafikverket_key_wrong),
                                    icon = R.drawable.key,
                                    duration = SnackbarDuration.Short
                                )
                            )
                        } else {
                            mainViewModel.settings.user.setApiKey(
                                ApiKey.Available(
                                    realtimeKey = realtimeKey,
                                    trafikVerketKey = trafikVerketKey
                                )
                            )

                            LavenderSnackbarController.pushEvent(
                                LavenderSnackbarEvents.MessageEvent(
                                    message = resources.getString(R.string.login_successful),
                                    icon = R.drawable.check,
                                    duration = SnackbarDuration.Short
                                )
                            )
                        }
                    }
                },
                enabled = realtimeKey.isNotBlank() && trafikVerketKey.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                Text(
                    text = stringResource(id = R.string.login_continue),
                    fontSize = TextStylingConstants.SIZE_MEDIUM
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = TextStylingConstants.SIZE_LARGE
            )
        },
        modifier = modifier
    )
}