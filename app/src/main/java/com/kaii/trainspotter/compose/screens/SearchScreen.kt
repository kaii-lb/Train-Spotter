package com.kaii.trainspotter.compose.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trainspotter.LocalMainViewModel
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.rememberSearchManager
import com.kaii.trainspotter.compose.widgets.SearchField
import com.kaii.trainspotter.compose.widgets.SearchItem
import com.kaii.trainspotter.compose.widgets.SearchItemPositon
import com.kaii.trainspotter.compose.widgets.SearchMode
import com.kaii.trainspotter.compose.widgets.SearchShimmerLoadingItem
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.TextStylingConstants

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
            val searchManager = rememberSearchManager(apiKey = apiKey)

            val mainViewModel = LocalMainViewModel.current
            val navController = LocalNavController.current
            val resources = LocalResources.current
            val listState = rememberLazyListState()

            var searchedText by remember { mutableStateOf("") }
            val searchMode by mainViewModel.searchMode.collectAsStateWithLifecycle()

            SearchField(
                text = searchedText,
                searchMode = searchMode,
                isError = searchManager.results.isEmpty(),
                setText = {
                    searchedText = it
                },
                setSearchMode = { mode ->
                    mainViewModel.searchMode.value = mode
                },
                onSearch = {
                    if (searchedText.isBlank()) {
                        searchManager.clearResults()
                    } else {
                        searchManager.search(
                            name = searchedText,
                            searchMode = searchMode,
                            resources = resources
                        )
                        listState.requestScrollToItem(0)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                state = listState
            ) {
                items(
                    count = searchManager.results.size + if (searchManager.isSearching) 10 else 0,
                    key = { index ->
                        if (index in 0..searchManager.results.size - 1) {
                            searchManager.results[index].id
                        } else {
                            index // fallback, shouldn't ever be here
                        }
                    }
                ) { index ->
                    AnimatedContent(
                        targetState = searchManager.isSearching,
                        transitionSpec = {
                            fadeIn().togetherWith(fadeOut())
                        }
                    ) { state ->
                        if (state) {
                            SearchShimmerLoadingItem(
                                position =
                                    when (index) {
                                        0 -> SearchItemPositon.Top
                                        searchManager.results.size + 9 -> SearchItemPositon.Bottom
                                        else -> SearchItemPositon.Middle
                                    }
                            )
                        } else {
                            if (index in 0..searchManager.results.size - 1) {
                                val item = searchManager.results[index]

                                if (item.mode == SearchMode.Station) {
                                    SearchItem(
                                        name = item.name,
                                        description = item.description,
                                        hasError = item.hasError,
                                        position =
                                            if (searchManager.results.size == 1) SearchItemPositon.Single
                                            else if (index == 0) SearchItemPositon.Top
                                            else if (index == searchManager.results.size - 1) SearchItemPositon.Bottom
                                            else SearchItemPositon.Middle,
                                        modifier = Modifier
                                            .animateItem()
                                    ) {
                                        navController.navigate(
                                            route = Screens.TimeTable(
                                                stopName = item.name,
                                                stopId = item.id
                                            )
                                        )
                                    }
                                } else {
                                    SearchItem(
                                        name = item.name,
                                        description = item.description,
                                        position = SearchItemPositon.Single,
                                        hasError = item.hasError,
                                        modifier = Modifier
                                            .animateItem()
                                    ) {
                                        navController.navigate(
                                            route = Screens.TrainDetails(
                                                trainId = item.id
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (searchManager.results.isEmpty() && !searchManager.isSearching) {
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