package com.kaii.trafikverkettracker.compose.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.LocalNavController
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.api.RealtimeClient
import com.kaii.trafikverkettracker.api.StopGroup
import com.kaii.trafikverkettracker.compose.widgets.StopSearchField
import com.kaii.trafikverkettracker.compose.widgets.StopSearchItem
import com.kaii.trafikverkettracker.compose.widgets.StopSearchItemPositon
import com.kaii.trafikverkettracker.helpers.Screens
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StopSearchScreen(
    apiKey: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar()
        },
        modifier = modifier
            .padding(8.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            var searchedText by remember { mutableStateOf("") }
            val stopGroups = remember { mutableStateListOf<StopGroup>() }

            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            val realtimeClient = remember {
                RealtimeClient(
                    context = context,
                    apiKey = apiKey
                )
            }

            StopSearchField(
                text = searchedText,
                setText = {
                    searchedText = it
                },
                placeholder = "Skurup Station",
                icon = R.drawable.location_on,
                onSearch = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val new = realtimeClient.findStopGroups(name = searchedText)?.stopGroups ?: emptyList()
                        stopGroups.clear()
                        stopGroups.addAll(new)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val navController = LocalNavController.current
            LazyColumn {
                items(
                    count = stopGroups.size,
                    key = { index ->
                        stopGroups[index].id
                    }
                ) { index ->
                    val stopGroup = stopGroups[index]

                    StopSearchItem(
                        stop = stopGroup,
                        position =
                            if (stopGroups.size == 1) StopSearchItemPositon.Single
                            else if (index == 0) StopSearchItemPositon.Top
                            else if (index == stopGroups.size - 1) StopSearchItemPositon.Bottom
                            else StopSearchItemPositon.Middle,
                        modifier = Modifier
                            .animateItem()
                    ) {
                        navController.navigate(
                            route = Screens.TimeTableScreen(
                                apiKey = apiKey,
                                stopGroup = stopGroup
                            )
                        )
                    }
                }
            }

            if (stopGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 48.dp
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.wrong_location),
                        contentDescription = "No such location found",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(56.dp)
                    )
                }
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
                text = stringResource(id = R.string.search),
                fontSize = TextStylingConstants.SIZE_LARGE
            )
        },
        modifier = modifier
    )
}