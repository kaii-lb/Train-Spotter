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
import androidx.compose.material3.IconButton
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
import com.kaii.trafikverkettracker.api.LocationDetails
import com.kaii.trafikverkettracker.api.RealtimeClient
import com.kaii.trafikverkettracker.api.StopGroup
import com.kaii.trafikverkettracker.api.TrafikVerketClient
import com.kaii.trafikverkettracker.compose.widgets.SearchField
import com.kaii.trafikverkettracker.compose.widgets.SearchItem
import com.kaii.trafikverkettracker.compose.widgets.SearchItemPositon
import com.kaii.trafikverkettracker.compose.widgets.SearchMode
import com.kaii.trafikverkettracker.compose.widgets.SearchShimmerLoadingItem
import com.kaii.trafikverkettracker.compose.widgets.StopSearchItem
import com.kaii.trafikverkettracker.datastore.ApiKey
import com.kaii.trafikverkettracker.helpers.Screens
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    apiKey: ApiKey.Available,
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
                    apiKey = apiKey.realtimeKey
                )
            }
            val trafikVerketClient = remember {
                TrafikVerketClient(
                    context = context,
                    apiKey = apiKey.trafikVerketKey
                )
            }

            val navController = LocalNavController.current
            var isError by remember { mutableStateOf(false) }
            var searchMode by remember { mutableStateOf(SearchMode.Station) }
            var foundTrain by remember { mutableStateOf<Pair<LocationDetails, LocationDetails>?>(null) }
            var isSearching by remember { mutableStateOf(false) }

            SearchField(
                text = searchedText,
                searchMode = searchMode,
                isError = isError,
                setText = {
                    isError = false
                    searchedText = it
                },
                setSearchMode = { mode ->
                    searchMode = mode
                },
                onSearch = {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (searchedText.isBlank()) {
                            foundTrain = null
                            stopGroups.clear()

                            return@launch
                        }

                        isSearching = true
                        stopGroups.clear()
                        foundTrain = null

                        if (searchMode == SearchMode.Station) {
                            val new = realtimeClient.findStopGroups(name = searchedText.trim())?.stopGroups ?: emptyList()

                            isError = new.isEmpty()

                            stopGroups.addAll(new - stopGroups)
                            stopGroups.retainAll(new)
                        } else {
                            val new = trafikVerketClient.getRouteDataForId(trainId = searchedText)

                            isError = new.isEmpty()

                            if (new.isNotEmpty()) foundTrain = Pair(new.values.first(), new.values.last())
                        }
                        isSearching = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                if (isSearching) {
                    items(
                        count = 10
                    ) { index ->
                        SearchShimmerLoadingItem(
                            position =
                                if (stopGroups.size == 1) SearchItemPositon.Single
                                else if (index == 0) SearchItemPositon.Top
                                else if (index == stopGroups.size - 1) SearchItemPositon.Bottom
                                else SearchItemPositon.Middle,
                        )
                    }
                }


                items(
                    count = stopGroups.size,
                    key = { index ->
                        if (index in 0..stopGroups.size - 1) {
                            stopGroups[index].id
                        } else {
                            index
                        }
                    }
                ) { index ->
                    if (index in 0..stopGroups.size - 1) {
                        val stopGroup = stopGroups[index]

                        StopSearchItem(
                            stop = stopGroup,
                            position =
                                if (stopGroups.size == 1) SearchItemPositon.Single
                                else if (index == 0) SearchItemPositon.Top
                                else if (index == stopGroups.size - 1) SearchItemPositon.Bottom
                                else SearchItemPositon.Middle,
                            modifier = Modifier
                                .animateItem()
                        ) {
                            navController.navigate(
                                route = Screens.TimeTable(
                                    stopGroup = stopGroup
                                )
                            )
                        }
                    }
                }

                foundTrain?.let {
                    item {
                        SearchItem(
                            name = stringResource(
                                id = R.string.search_train_route,
                                it.first.name,
                                it.second.name
                            ),
                            description = stringResource(
                                id = R.string.search_train_time,
                                it.first.departureTimeFormatted,
                                it.second.arrivalTimeFormatted
                            ),
                            position = SearchItemPositon.Single
                        ) {
                            navController.navigate(
                                route = Screens.TrainDetails(
                                    trainId = searchedText.trim()
                                )
                            )
                        }
                    }
                }
            }

            if (isError) {
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
                        painter = painterResource(
                            id =
                                if (searchMode == SearchMode.Station) R.drawable.wrong_location
                                else R.drawable.train_not_found
                        ),
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
    val navController = LocalNavController.current

    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.search),
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