package com.kaii.trafikverkettracker.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.LocalNavController
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.api.ArrivalsResponse
import com.kaii.trafikverkettracker.api.DeparturesResponse
import com.kaii.trafikverkettracker.api.RealtimeClient
import com.kaii.trafikverkettracker.api.StopGroup
import com.kaii.trafikverkettracker.compose.widgets.TimeTableElement
import com.kaii.trafikverkettracker.compose.widgets.TimeTableType
import com.kaii.trafikverkettracker.compose.widgets.TimeTableTypeDisplay
import com.kaii.trafikverkettracker.helpers.Screens
import com.kaii.trafikverkettracker.helpers.ServerConstants
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class, ExperimentalComposeApi::class)
@Composable
fun TimeTableScreen(
    apiKey: String,
    stopGroup: StopGroup,
    modifier: Modifier = Modifier
) {
    var type by remember { mutableStateOf(TimeTableType.Arrivals) }

    Scaffold(
        topBar = {
            TopBar(
                stopName = stopGroup.name,
                type = type,
                setType = {
                    type = it
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val listState = rememberLazyListState()
        var departures: DeparturesResponse? by remember { mutableStateOf(null) }
        var arrivals: ArrivalsResponse? by remember { mutableStateOf(null) }

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val realtimeClient = remember {
            RealtimeClient(
                context = context,
                apiKey = apiKey
            )
        }

        var previousType by remember { mutableStateOf(type) }
        LaunchedEffect(type) {
            while (true) {
                if (type == TimeTableType.Arrivals) {
                    arrivals = realtimeClient.fetchArrivals(stopGroup.id)
                } else {
                    departures = realtimeClient.fetchDepartures(stopGroup.id)
                }

                if (previousType != type && listState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    listState.scrollToItem(0)
                    previousType = type
                }

                delay(ServerConstants.UPDATE_TIME)
            }
        }

        var isRefreshing by remember { mutableStateOf(false) }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch(Dispatchers.IO) {
                    isRefreshing = true

                    if (type == TimeTableType.Arrivals) {
                        arrivals = realtimeClient.fetchArrivals(stopGroup.id)
                    } else {
                        departures = realtimeClient.fetchDepartures(stopGroup.id)
                    }

                    delay(ServerConstants.REFRESH_TIME)

                    isRefreshing = false
                }
            },
            modifier = Modifier
                .padding(innerPadding)
        ) {
            JetLimeColumn(
                listState = listState,
                itemsList = ItemsList(
                    if (type == TimeTableType.Arrivals) arrivals?.arrivals ?: emptyList()
                    else departures?.departures ?: emptyList()
                ),
                style = JetLimeDefaults.columnStyle(
                    itemSpacing = 16.dp,
                ),
                contentPadding = PaddingValues(16.dp)
            ) { _, item, position ->
                TimeTableElement(
                    item = item,
                    position = position
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    stopName: String,
    type: TimeTableType,
    setType: (type: TimeTableType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
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
                    text = stopName,
                    fontSize = TextStylingConstants.SIZE_LARGE
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(
                            route = Screens.Settings
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Start settings"
                    )
                }
            },
            modifier = modifier
        )

        TimeTableTypeDisplay(
            type = type,
            setType = setType
        )
    }
}