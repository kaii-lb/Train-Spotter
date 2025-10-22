package com.kaii.trafikverkettracker.compose.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.kaii.trafikverkettracker.api.LocationDetails
import com.kaii.trafikverkettracker.api.TrafikVerketClient
import com.kaii.trafikverkettracker.compose.widgets.TrainDetailTableElement
import com.kaii.trafikverkettracker.helpers.Screens
import com.kaii.trafikverkettracker.helpers.ServerConstants
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrainDetailsScreen(
    apiKey: String,
    trainId: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar(
                trainId = trainId
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val trafikVerketClient = remember {
            TrafikVerketClient(
                context = context,
                apiKey = apiKey
            )
        }

        val listState = rememberLazyListState()
        val announcementsKeys = remember { mutableStateListOf<String>() } // so its sorted
        val announcements = remember { mutableStateMapOf<String, LocationDetails>() }

        LaunchedEffect(Unit) {
            while (true) {
                val new = trafikVerketClient.getRouteDataForId(trainId = trainId)

                announcements.clear()
                announcements.putAll(new)

                announcementsKeys.clear()
                announcementsKeys.addAll(new.keys)

                delay(ServerConstants.UPDATE_TIME)
            }
        }

        var isRefreshing by remember { mutableStateOf(false) }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch(Dispatchers.IO) {
                    isRefreshing = true

                    val new = trafikVerketClient.getRouteDataForId(trainId = trainId)

                    announcements.clear()
                    announcements.putAll(new)

                    announcementsKeys.clear()
                    announcementsKeys.addAll(new.keys)

                    delay(ServerConstants.REFRESH_TIME)

                    isRefreshing = false
                }
            },
            modifier = Modifier
                .padding(innerPadding)
        ) {
            JetLimeColumn(
                listState = listState,
                itemsList = ItemsList(announcementsKeys),
                style = JetLimeDefaults.columnStyle(
                    itemSpacing = 16.dp,
                ),
                contentPadding = PaddingValues(16.dp)
            ) { _, item, position ->
                TrainDetailTableElement(
                    item = announcements[item]!!,
                    position = position
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    trainId: String,
    modifier: Modifier = Modifier
) {
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
                text = "Train with ID: $trainId",
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
}