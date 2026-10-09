package com.kaii.trainspotter.compose.widgets.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kaii.trainspotter.compose.widgets.SearchShimmerLoadingItem
import com.kaii.trainspotter.domain.search.SearchMode
import com.kaii.trainspotter.domain.search.SearchResult
import com.kaii.trainspotter.domain.station.TransportMode
import com.kaii.trainspotter.helpers.ResultInfoFetcher

@Composable
fun LazyItemScope.CompleteSearchItem(
    index: Int,
    listSize: Int,
    item: SearchResult,
    isSearching: () -> Boolean,
    infoFetcher: ResultInfoFetcher,
    onClick: () -> Unit
) {
    AnimatedContent(
        targetState = isSearching(),
        transitionSpec = {
            val enter = fadeIn(animationSpec = tween(durationMillis = 300)) + expandVertically(animationSpec = tween(durationMillis = 600))
            val exit = (fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically(animationSpec = tween(durationMillis = 600)))

            enter.togetherWith(exit)
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateItem(
                fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                fadeOutSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                placementSpec = MaterialTheme.motionScheme.fastSpatialSpec()
            )
    ) { state ->
        if (state) {
            SearchShimmerLoadingItem(
                position =
                    when (index) {
                        0 -> SearchItemPositon.Top
                        listSize - 1 -> SearchItemPositon.Bottom
                        else -> SearchItemPositon.Middle
                    }
            )
        } else {
            if (item.mode == SearchMode.Station) {
                SearchItem(
                    name = remember(item) { infoFetcher.getFromName(item.name) },
                    description = remember(item) { infoFetcher.getFromDescription(item.description) },
                    hasError = item.hasError,
                    transportMode = remember(item) {
                        if (item.transportModes.contains(TransportMode.Train)) TransportMode.Train
                        else item.transportModes.first()
                    },
                    position =
                        if (listSize == 1) SearchItemPositon.Single
                        else if (index == 0) SearchItemPositon.Top
                        else if (index == listSize - 1) SearchItemPositon.Bottom
                        else SearchItemPositon.Middle,
                    onClick = onClick
                )
            } else {
                SearchItem(
                    name = remember(item) { infoFetcher.getFromName(item.name) },
                    description = remember(item) { infoFetcher.getFromDescription(item.description) },
                    position = SearchItemPositon.Single,
                    hasError = item.hasError,
                    transportMode = item.transportModes.first(),
                    onClick = onClick
                )
            }
        }
    }
}