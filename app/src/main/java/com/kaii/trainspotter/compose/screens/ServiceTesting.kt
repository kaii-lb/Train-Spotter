package com.kaii.trainspotter.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaii.lavender.snackbars.LavenderSnackbarController
import com.kaii.lavender.snackbars.LavenderSnackbarEvents
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.PreferenceRow
import com.kaii.trainspotter.compose.widgets.PreferencesSeparatorText
import com.kaii.trainspotter.compose.widgets.TextPreferencesRow
import com.kaii.trainspotter.data.TrainUpdateService
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.models.ServiceTestingViewModel
import com.kaii.trainspotter.presentation.RowPosition
import kotlinx.coroutines.launch

@Composable
fun ServiceTesting(
    viewModel: ServiceTestingViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBar()
        }
    ) { innerPadding ->
        var trainId by remember { mutableStateOf("") }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.Top
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                PreferencesSeparatorText(
                    text = stringResource(id = R.string.service_testing_management)
                )
            }

            item {
                PreferenceRow(
                    title = stringResource(id = R.string.service_testing_train_id),
                    icon = R.drawable.id_card,
                    position = RowPosition.Single
                ) {
                    TextField(
                        value = trainId,
                        onValueChange = { new ->
                            trainId = new
                        },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.service_testing_train_id_placeholder)
                            )
                        },
                        singleLine = true,
                        isError = trainId.isBlank(),
                        shape = RoundedCornerShape(RoundedCornerConstants.ROUNDING_EXTRA_LARGE),
                        colors = TextFieldDefaults.colors(
                            errorIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            item {
                val resources = LocalResources.current
                val coroutineScope = rememberCoroutineScope()

                TextPreferencesRow(
                    title = stringResource(id = R.string.service_testing_start),
                    icon = R.drawable.play_arrow,
                    text = stringResource(id = R.string.service_testing_start_desc),
                    clearBackground = true,
                    position = RowPosition.Top
                ) {
                    if (trainId.isNotBlank()) {
                        viewModel.start(trainId.trim())
                        TrainUpdateService.start(context)
                    } else {
                        coroutineScope.launch {
                            val message = resources.getString(R.string.service_testing_train_id_invalid)
                            LavenderSnackbarController.pushEvent(
                                LavenderSnackbarEvents.MessageEvent(
                                    message = message,
                                    icon = R.drawable.exclamation,
                                    duration = SnackbarDuration.Short
                                )
                            )
                        }
                    }
                }
            }

            item {
                TextPreferencesRow(
                    title = stringResource(id = R.string.service_testing_stop),
                    icon = R.drawable.stop,
                    text = stringResource(id = R.string.service_testing_stop_desc),
                    clearBackground = true,
                    position = RowPosition.Bottom,
                    onClick = viewModel::stop
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
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
                text = stringResource(id = R.string.service_testing),
                fontSize = TextStylingConstants.SIZE_LARGE
            )
        }
    )
}