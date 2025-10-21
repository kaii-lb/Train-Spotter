package com.kaii.trafikverkettracker.compose.screens

import android.content.Intent
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.net.toUri
import com.kaii.trafikverkettracker.LocalMainViewModel
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.helpers.RoundedCornerConstants
import com.kaii.trafikverkettracker.helpers.TextStylingConstants

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
            var apiKey by rememberSaveable { mutableStateOf("") }
            val isError by remember {
                derivedStateOf {
                    apiKey.isBlank()
                }
            }

            TextField(
                value = apiKey,
                onValueChange = { new ->
                    apiKey = new
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_api_key)
                    )
                },
                prefix = {
                    Icon(
                        painter = painterResource(id = R.drawable.key),
                        contentDescription = stringResource(id = R.string.login_api_key),
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                },
                singleLine = true,
                isError = isError,
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

            val context = LocalContext.current
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
                        val intent = Intent().apply {
                            data = "https://developer.trafiklab.se".toUri()
                            action = Intent.ACTION_VIEW
                        }

                        context.startActivity(intent)
                    }
            )

            val mainViewModel = LocalMainViewModel.current
            Button(
                onClick = {
                    mainViewModel.settings.user.setApiKey(apiKey)
                },
                enabled = !isError,
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