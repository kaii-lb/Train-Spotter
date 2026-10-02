package com.kaii.trainspotter.compose.widgets.search

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFieldError(
    isQueryEmpty: () -> Boolean,
    isError: () -> Boolean,
    modifier: Modifier = Modifier,
    onClear: () -> Unit
) {
    AnimatedContent(
        targetState =
            if (isError()) 1
            else if (!isQueryEmpty()) 2
            else 0,
        transitionSpec = {
            (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
        },
        contentAlignment = Alignment.Center,
        modifier = modifier
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
                        .clickable(onClick = onClear)
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
}