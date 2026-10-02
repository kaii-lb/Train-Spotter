package com.kaii.trainspotter.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.search.CompleteSearchItem
import com.kaii.trainspotter.compose.widgets.search.SearchFieldError
import com.kaii.trainspotter.compose.widgets.search.SearchItem
import com.kaii.trainspotter.compose.widgets.search.SearchItemPositon
import com.kaii.trainspotter.compose.widgets.search.SearchModeSelector
import com.kaii.trainspotter.domain.SearchDescription
import com.kaii.trainspotter.domain.SearchMode
import com.kaii.trainspotter.domain.SearchName
import com.kaii.trainspotter.domain.SearchResult
import com.kaii.trainspotter.domain.TransportMode
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.rememberResultInfoFetcher
import com.kaii.trainspotter.models.SearchViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private val placeholders = listOf(
    "Skurup Station",
    "Malmö CentralStation",
    "1778",
    "1727"
)

@Preview
@Composable
private fun SearchScreenPreview() {
    val textFieldState = rememberTextFieldState()
    var searchMode by remember { mutableStateOf(SearchMode.Station) }

    SearchScreen(
        results = {
            List(5) {
                val mode = TransportMode.entries.random()

                SearchResult(
                    name = SearchName.Station(it.toString()),
                    description = SearchDescription.Station(modes = listOf(mode)),
                    id = it.toString(),
                    hasError = false,
                    mode = SearchMode.Station,
                    transportModes = listOf(mode)
                )
            }.filter {
                when (it.name) {
                    is SearchName.Station -> it.name.name.contains(textFieldState.text)
                    is SearchName.TrainRoute -> it.name.start.contains(textFieldState.text) || it.name.end.contains(textFieldState.text)
                }
            }
        },
        isSearching = { false },
        searchMode = { searchMode },
        history = {
            List(3) {
                val mode = TransportMode.entries.random()

                SearchResult(
                    name = SearchName.Station(it.toString()),
                    description = SearchDescription.Station(modes = listOf(mode)),
                    id = it.toString(),
                    hasError = false,
                    mode = SearchMode.Station,
                    transportModes = listOf(mode)
                )
            }
        },
        navController = rememberNavController(),
        onAddToHistory = {},
        onSearchModeChange = { searchMode = it },
        onSearch = {}
    )
}

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
        onSearch = viewModel::search
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
    textFieldState: TextFieldState = rememberTextFieldState(),
    onAddToHistory: (item: SearchResult) -> Unit,
    onSearchModeChange: (mode: SearchMode) -> Unit,
    onSearch: (query: String) -> Unit
) {
    val listState = rememberLazyListState()
    val infoFetcher = rememberResultInfoFetcher()

    Scaffold(
        topBar = {
            TopBar(
                searchMode = searchMode,
                isError = { results().isEmpty() && textFieldState.text.isNotBlank() && !isSearching() },
                textFieldState = textFieldState,
                navController = navController,
                onSearchModeChange = onSearchModeChange,
                onSearch = {
                    onSearch(it)
                    listState.requestScrollToItem(0)
                }
            )
        },
        modifier = modifier
            .padding(8.dp)
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(all = 8.dp)
        ) {
            Text(
                text = stringResource(
                    id =
                        if (results().isNotEmpty()) R.string.results
                        else R.string.history
                ),
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                itemsIndexed(
                    items = results(),
                    key = { _, item -> item.id }
                ) { index, item ->
                    CompleteSearchItem(
                        index = index,
                        listSize = results().size,
                        item = item,
                        isSearching = isSearching,
                        infoFetcher = infoFetcher,
                        onClick = {
                            if (item.mode == SearchMode.Station) {
                                onAddToHistory(item)
                                navController.navigate(
                                    route = Screens.TimeTable(
                                        stopName = infoFetcher.getFromName(item.name),
                                        stopId = item.id
                                    )
                                )
                            } else {
                                navController.navigate(
                                    route = Screens.TrainDetails(
                                        trainId = item.id
                                    )
                                )
                            }
                        }
                    )
                }

                if (results().isEmpty() && !isSearching()) {
                    if (searchMode() == SearchMode.Station && !history().isEmpty() && textFieldState.text.isBlank()) {
                        itemsIndexed(
                            items = history(),
                            key = { _, item -> item.id }
                        ) { index, item ->
                            SearchItem(
                                name = remember(item) { infoFetcher.getFromName(item.name) },
                                description = remember(item) { infoFetcher.getFromDescription(item.description) },
                                hasError = item.hasError,
                                transportMode = item.transportModes.first(),
                                position =
                                    if (history().size == 1) SearchItemPositon.Single
                                    else if (index == 0) SearchItemPositon.Top
                                    else if (index == history().size - 1) SearchItemPositon.Bottom
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(space = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
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
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(all = 16.dp)
                                )

                                Text(
                                    text = stringResource(
                                        id =
                                            if (searchMode() == SearchMode.Station) R.string.search_station_not_found
                                            else R.string.search_train_not_found
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun TopBar(
    searchMode: () -> SearchMode,
    isError: () -> Boolean,
    textFieldState: TextFieldState,
    navController: NavController,
    modifier: Modifier = Modifier,
    onSearchModeChange: (SearchMode) -> Unit,
    onSearch: (String) -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            val keyboardController = LocalSoftwareKeyboardController.current

            LaunchedEffect(textFieldState, searchMode()) {
                snapshotFlow { textFieldState.text }
                    .debounce(500.milliseconds)
                    .distinctUntilChanged()
                    .collectLatest {
                        onSearch(textFieldState.text.toString())
                    }
            }

            TextField(
                state = textFieldState,
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.tonalColors(),
                isError = isError(),
                shape = CircleShape,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                onKeyboardAction = {
                    onSearch(textFieldState.text.toString())
                    keyboardController?.hide()
                },
                trailingIcon = {
                    SearchFieldError(
                        isQueryEmpty = { textFieldState.text.isBlank() },
                        isError = isError,
                        onClear = {
                            textFieldState.clearText()
                            onSearch("")
                        }
                    )
                },
                placeholder = {
                    Text(
                        text = remember(searchMode()) {
                            placeholders[Random.nextInt(
                                if (searchMode() == SearchMode.Station) 0 else placeholders.size / 2,
                                if (searchMode() == SearchMode.Station) placeholders.size / 2 else placeholders.size
                            )]
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            )
        },
        navigationIcon = {
            SearchModeSelector(
                searchMode = searchMode,
                onSearchModeChange = onSearchModeChange
            )
        },
        actions = {
            FilledIconButton(
                onClick = {
                    navController.navigate(
                        route = Screens.Settings
                    )
                },
                colors = IconButtonDefaults.iconButtonVibrantColors(),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Start settings"
                )
            }
        }
    )
}
