package com.kaii.trainspotter.compose.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.PreferencesSeparatorText
import com.kaii.trainspotter.compose.widgets.SearchField
import com.kaii.trainspotter.compose.widgets.SearchItem
import com.kaii.trainspotter.compose.widgets.SearchItemPositon
import com.kaii.trainspotter.compose.widgets.SearchShimmerLoadingItem
import com.kaii.trainspotter.domain.SearchMode
import com.kaii.trainspotter.domain.SearchResult
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.helpers.rememberResultInfoFetcher
import com.kaii.trainspotter.models.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchMode by viewModel.searchMode.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    SearchScreen(
        results = { results },
        isSearching = { isSearching },
        searchMode = { searchMode },
        history = { history },
        modifier = modifier,
        navController = LocalNavController.current,
        onAddToHistory = viewModel::addToHistory,
        onSearchModeChange = viewModel::changeSearchMode,
        onSearch = viewModel::search,
        onClear = viewModel::clear
    )
}

@Composable
private fun SearchScreen(
    results: () -> List<SearchResult>,
    isSearching: () -> Boolean,
    searchMode: () -> SearchMode,
    history: () -> List<SearchResult>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onAddToHistory: (item: SearchResult) -> Unit,
    onSearchModeChange: (mode: SearchMode) -> Unit,
    onSearch: (query: String) -> Unit,
    onClear: () -> Unit
) {
    val results by rememberUpdatedState(results())
    val history by rememberUpdatedState(history())
    val infoFetcher = rememberResultInfoFetcher()

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
            val listState = rememberLazyListState()
            var searchedText by rememberSaveable { mutableStateOf("") }

            SearchField(
                text = searchedText,
                searchMode = searchMode,
                isError = results.isEmpty(),
                setText = {
                    searchedText = it
                },
                setSearchMode = onSearchModeChange,
                onSearch = {
                    if (searchedText.isBlank()) {
                        onClear()
                    } else {
                        onSearch(searchedText)
                        listState.requestScrollToItem(0)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                state = listState
            ) {
                items(
                    count = results.size,
                    key = { index ->
                        if (index in results.indices) {
                            results[index].id
                        } else {
                            index // fallback, shouldn't ever be here
                        }
                    }
                ) { index ->
                    AnimatedContent(
                        targetState = isSearching(),
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(durationMillis = 300)) + expandVertically(animationSpec = tween(durationMillis = 600)))
                                .togetherWith(
                                    (fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically(animationSpec = tween(durationMillis = 600)))
                                )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    ) { state ->
                        if (state) {
                            SearchShimmerLoadingItem(
                                position =
                                    when (index) {
                                        0 -> SearchItemPositon.Top
                                        results().size - 1 -> SearchItemPositon.Bottom
                                        else -> SearchItemPositon.Middle
                                    }
                            )
                        } else {
                            val item = results.getOrNull(index) ?: return@AnimatedContent

                            if (item.mode == SearchMode.Station) {
                                SearchItem(
                                    name = infoFetcher.getFromName(item.name),
                                    description = infoFetcher.getFromDescription(item.description),
                                    hasError = item.hasError,
                                    position =
                                        if (results.size == 1) SearchItemPositon.Single
                                        else if (index == 0) SearchItemPositon.Top
                                        else if (index == results.size - 1) SearchItemPositon.Bottom
                                        else SearchItemPositon.Middle
                                ) {
                                    onAddToHistory(item)
                                    navController.navigate(
                                        route = Screens.TimeTable(
                                            stopName = infoFetcher.getFromName(item.name),
                                            stopId = item.id
                                        )
                                    )
                                }
                            } else {
                                SearchItem(
                                    name = infoFetcher.getFromName(item.name),
                                    description = infoFetcher.getFromDescription(item.description),
                                    position = SearchItemPositon.Single,
                                    hasError = item.hasError
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

                if (results.isEmpty() && !isSearching()) {
                    if (searchMode() == SearchMode.Station && !history.isEmpty()) {
                        item {
                            PreferencesSeparatorText(
                                text = stringResource(id = R.string.history),
                                align = TextAlign.Center,
                                modifier = Modifier
                                    .padding(
                                        start = 12.dp, end = 12.dp,
                                        top = 0.dp, bottom = 12.dp
                                    )
                            )
                        }

                        itemsIndexed(
                            items = history
                        ) { index, item ->
                            SearchItem(
                                name = infoFetcher.getFromName(item.name),
                                description = infoFetcher.getFromDescription(item.description),
                                hasError = item.hasError,
                                position =
                                    if (history.size == 1) SearchItemPositon.Single
                                    else if (index == 0) SearchItemPositon.Top
                                    else if (index == history.size - 1) SearchItemPositon.Bottom
                                    else SearchItemPositon.Middle
                            ) {
                                onAddToHistory(item)
                                navController.navigate(
                                    route = Screens.TimeTable(
                                        stopName = infoFetcher.getFromName(item.name),
                                        stopId = item.id
                                    )
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 48.dp
                                    )
                                    .animateItem(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id =
                                            if (searchMode() == SearchMode.Station) R.drawable.wrong_location_filled
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
