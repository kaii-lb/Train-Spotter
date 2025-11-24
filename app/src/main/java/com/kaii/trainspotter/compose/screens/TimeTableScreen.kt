package com.kaii.trainspotter.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.TableShimmerLoadingElement
import com.kaii.trainspotter.compose.widgets.TimeTableElement
import com.kaii.trainspotter.compose.widgets.TimeTableType
import com.kaii.trainspotter.compose.widgets.TimeTableTypeDisplay
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.models.time_table.TimeTableViewModel
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class, ExperimentalComposeApi::class)
@Composable
fun TimeTableScreen(
    stopName: String,
    stopId: String,
    viewModel: TimeTableViewModel,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    BackHandler {
        viewModel.cancel()
        navController.popBackStack()
    }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        viewModel.startListening(
            stopId = stopId,
            onScroll = { index ->
                listState.scrollToItem(0)
            }
        )
    }

    val type by viewModel.type.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopBar(
                stopName = stopName,
                type = type,
                setType = { newType ->
                    viewModel.switchType(
                        type = newType,
                        onScroll = { index ->
                            listState.scrollToItem(0)
                        }
                    )
                },
                onBackClick = viewModel::cancel
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.forceRefresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // because for some reason mutable state flows do not like to be joined and update frequently
            val arrivals by viewModel.arrivals.collectAsStateWithLifecycle()
            val departures by viewModel.departures.collectAsStateWithLifecycle()

            val items by remember {
                derivedStateOf {
                    if (type == TimeTableType.Arrivals) arrivals?.arrivals ?: emptyList()
                    else departures?.departures ?: emptyList()
                }
            }

            JetLimeColumn(
                listState = listState,
                itemsList = ItemsList(
                    items =
                        if (isRefreshing && items.isEmpty()) viewModel.placeholderItems
                        else items
                ),
                style = JetLimeDefaults.columnStyle(
                    itemSpacing = 16.dp,
                ),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxSize()
            ) { _, item, position ->
                AnimatedContent(
                    targetState = isRefreshing,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    modifier = Modifier
                        .fillMaxSize()
                ) { state ->
                    if (state) {
                        TableShimmerLoadingElement(
                            position = position
                        )
                    } else {
                        TimeTableElement(
                            item = item,
                            position = position
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    stopName: String,
    type: TimeTableType,
    modifier: Modifier = Modifier,
    setType: (type: TimeTableType) -> Unit,
    onBackClick: () -> Unit
) {
    Column {
        val navController = LocalNavController.current

        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = {
                        onBackClick()
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