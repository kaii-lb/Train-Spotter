package com.kaii.trainspotter.compose.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Information
import com.kaii.trainspotter.api.LocationDetails
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainInformation
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.compose.widgets.TableShimmerLoadingElement
import com.kaii.trainspotter.compose.widgets.TrainDetailTableElement
import com.kaii.trainspotter.compose.widgets.TrainInfoDialog
import com.kaii.trainspotter.helpers.ServerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants
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
    val announcementsKeys = remember { mutableStateListOf<String>() } // so its sorted
    val announcements = remember { mutableStateMapOf<String, LocationDetails>() }

    Scaffold(
        topBar = {
            TopBar(
                trainId = trainId,
                apiKey = apiKey,
                productInfo = announcements.values.flatMap {
                    it.productInfo
                }.distinct()
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val trafikVerketClient = remember(apiKey) {
            TrafikverketClient(
                context = context,
                apiKey = apiKey
            )
        }

        val listState = rememberLazyListState()
        var isRefreshing by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            while (true) {
                val new = trafikVerketClient.getRouteDataForId(trainId = trainId)

                announcements.clear()
                announcements.putAll(new)

                announcementsKeys.clear()
                announcementsKeys.addAll(new.keys)
                isRefreshing = false

                delay(ServerConstants.UPDATE_TIME)
            }
        }


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
                itemsList = ItemsList(
                    if (isRefreshing && announcementsKeys.isEmpty()) {
                        (0..9).toList()
                    } else {
                        announcementsKeys
                    }
                ),
                style = JetLimeDefaults.columnStyle(
                    itemSpacing = 16.dp,
                ),
                contentPadding = PaddingValues(16.dp)
            ) { _, item, position ->
                AnimatedContent(
                    targetState = isRefreshing,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    }
                ) { state ->
                    if (state) {
                        TableShimmerLoadingElement(
                            position = position
                        )
                    } else if (announcements[item] != null) {
                        TrainDetailTableElement(
                            item = announcements[item]!!,
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
    trainId: String,
    apiKey: String,
    productInfo: List<Information>,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val trainPositionClient = remember(trainId) {
        TrainPositionClient(
            context = context,
            apiKey = apiKey
        )
    }

    var speed by rememberSaveable { mutableIntStateOf(-1) }
    val animatedSpeed by animateIntAsState(
        targetValue = speed,
        animationSpec = tween(
            durationMillis = 800
        )
    )

    var bearing by rememberSaveable { mutableIntStateOf(-1) }
    val animatedBearing by animateIntAsState(
        targetValue = bearing,
        animationSpec = tween(
            durationMillis = 800
        )
    )

    var speedIsEstimate by remember { mutableStateOf(false) }

    LifecycleStartEffect(trainId) {
        coroutineScope.launch(Dispatchers.IO) {
            trainPositionClient.getStreamingInfo(
                trainId = trainId
            ) { info ->
                speed = if (info.speed == null && info.position != null && info.timeStamp != null) {
                    speedIsEstimate = true

                    info.position.toCoords(info.timeStamp)?.let { current ->
                        val new = trainPositionClient.calcSpeed(
                            coords = current
                        )

                        new
                    } ?: 0
                } else {
                    speedIsEstimate = false
                    info.speed ?: -1
                }

                bearing = info.bearing ?: -1
            }
        }

        onStopOrDispose {
            trainPositionClient.cancel()
        }
    }

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
            var showDialog by remember { mutableStateOf(false) }
            val info = remember(productInfo) {
                productInfo.find {
                    it.code == TrainInformation.Product.type.toString()
                }
            }

            if (showDialog) {
                TrainInfoDialog(
                    info = productInfo,
                    onDismiss = {
                        showDialog = false
                    }
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        showDialog = true
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                val title by remember {
                    derivedStateOf {
                        val desc =
                            if (info != null) "${info.description} | $trainId"
                            else "Train: $trainId"

                        if (animatedSpeed == -1) {
                            desc
                        } else {
                            desc + " | " + animatedSpeed.toString() + "km/h" + if (speedIsEstimate) "*" else ""
                        }
                    }
                }

                Text(
                    text = title,
                    fontSize = TextStylingConstants.SIZE_LARGE
                )
            }
        },
        actions = {
            AnimatedVisibility(
                visible = animatedBearing != -1,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.compass),
                        tint = TopAppBarDefaults.topAppBarColors().titleContentColor,
                        contentDescription = "Start settings",
                        modifier = Modifier
                            .rotate(animatedBearing.toFloat())
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.needle_tip),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Start settings",
                        modifier = Modifier
                            .rotate(animatedBearing.toFloat())
                    )
                }
            }
        },
        modifier = modifier
    )
}