package com.kaii.trafikverkettracker.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.LocalMainViewModel
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.api.RealtimeClient
import com.kaii.trafikverkettracker.api.TrafikVerketClient
import com.kaii.trafikverkettracker.compose.widgets.ApiKeyExplanationDialog
import com.kaii.trafikverkettracker.datastore.ApiKey
import com.kaii.trafikverkettracker.helpers.RoundedCornerConstants
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
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
                            TrafikVerketClient(
                                context = context,
                                apiKey = trafikVerketKey
                            )

                        val response1 = realtimeClient.findStopGroups(name = "malmö")
                        val response2 = trafikVerketClient.getRouteDataForId(trainId = "1778")

                        if (response1 != null && response2.isNotEmpty()) {
                            mainViewModel.settings.user.setApiKey(
                                ApiKey.Available(
                                    realtimeKey = realtimeKey,
                                    trafikVerketKey = trafikVerketKey
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