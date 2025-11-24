package com.kaii.trainspotter.compose.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class SearchMode(
    @param:DrawableRes val icon: Int,
    @param:StringRes val description: Int
) {
    Station(
        icon = R.drawable.location_on_filled,
        description = R.string.station
    ),
    Train(
        icon = R.drawable.train_filled,
        description = R.string.train
    )
}

private val placeholders = listOf(
    "Skurup Station",
    "Malmo CentralStation",
    "1778",
    "1727"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    text: String,
    searchMode: SearchMode,
    isError: Boolean = false,
    setText: (text: String) -> Unit,
    setSearchMode: (mode: SearchMode) -> Unit,
    onSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(text, searchMode) {
        delay(2000)
        onSearch()
    }

    TextField(
        value = text,
        onValueChange = {
            setText(it)
        },
        isError = isError,
        maxLines = 1,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholders[Random.nextInt(
                    if (searchMode == SearchMode.Station) 0 else placeholders.size / 2,
                    if (searchMode == SearchMode.Station) placeholders.size / 2 else placeholders.size - 1
                )],
                fontSize = TextStylingConstants.SIZE_MEDIUM
            )
        },
        leadingIcon = {
            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                shape = RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_LARGE)
            ) {
                SearchMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = mode.description)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = mode.icon),
                                contentDescription = "Search for ${mode.name}"
                            )
                        },
                        onClick = {
                            setSearchMode(mode)
                            onSearch()
                            expanded = false
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            expanded = true
                        }
                        .padding(all = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = searchMode.icon),
                        contentDescription = "Searching for ${searchMode.name}"
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.arrow_drop_down),
                        contentDescription = "Show drop down"
                    )
                }
            }
        },
        trailingIcon = {
            AnimatedContent(
                targetState =
                    if (isError) 1
                    else if (text.isNotEmpty()) 2
                    else 0,
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 8.dp)
            ) { state ->
                when (state) {
                    1 -> {
                        val tooltipState = rememberTooltipState(isPersistent = true)
                        val coroutineScope = rememberCoroutineScope()

                        TooltipBox(
                            state = tooltipState,
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Start, 8.dp),
                            tooltip = {
                                PlainTooltip(
                                    caretShape = TooltipDefaults.caretShape(),
                                    content = {
                                        Text(
                                            text = stringResource(id = R.string.search_invalid_input)
                                        )
                                    }
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        coroutineScope.launch {
                                            if (tooltipState.isVisible) tooltipState.dismiss()
                                            else tooltipState.show()
                                        }
                                    }
                                    .padding(all = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.priority_high),
                                    contentDescription = "Invalid query"
                                )
                            }
                        }
                    }
                    2 -> {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    setText("")
                                    onSearch()
                                }
                                .padding(all = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Clear search query"
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            errorTrailingIconColor = MaterialTheme.colorScheme.error,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            errorLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                keyboardController?.hide()
            }
        ),
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
    )
}