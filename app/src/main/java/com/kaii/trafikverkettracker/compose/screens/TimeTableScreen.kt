package com.kaii.trafikverkettracker.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.api.ArrivalsResponse
import com.kaii.trafikverkettracker.api.DeparturesResponse
import com.kaii.trafikverkettracker.api.RealtimeClient
import com.kaii.trafikverkettracker.api.StopGroup
import com.kaii.trafikverkettracker.compose.widgets.TimeTableElement
import com.kaii.trafikverkettracker.compose.widgets.TimeTableType
import com.kaii.trafikverkettracker.compose.widgets.TimeTableTypeDisplay
import com.kaii.trafikverkettracker.helpers.ServerConstants
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        LaunchedEffect(type) {
            withContext(Dispatchers.IO) {
                while (true) {
                    if (type == TimeTableType.Arrivals) {
                        arrivals = realtimeClient.fetchArrivals(stopGroup.id)
                    } else {
                        departures = realtimeClient.fetchDepartures(stopGroup.id)
                    }

                    delay(ServerConstants.UPDATE_TIME)
                }
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
        TopAppBar(
            title = {
                Text(
                    text = stopName,
                    fontSize = TextStylingConstants.SIZE_LARGE
                )
            },
            modifier = modifier
        )

        TimeTableTypeDisplay(
            type = type,
            setType = setType
        )
    }
}