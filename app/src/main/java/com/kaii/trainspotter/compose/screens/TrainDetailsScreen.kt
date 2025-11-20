@file:Suppress("deprecation")

package com.kaii.trainspotter.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaii.trainspotter.LocalMainViewModel
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Information
import com.kaii.trainspotter.api.LocationDetails
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainInformation
import com.kaii.trainspotter.compose.widgets.TableShimmerLoadingElement
import com.kaii.trainspotter.compose.widgets.TrainDetailTableElement
import com.kaii.trainspotter.compose.widgets.TrainInfoDialog
import com.kaii.trainspotter.compose.widgets.shimmerEffect
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.ServerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.helpers.tintDrawable
import com.kaii.trainspotter.ui.theme.MapStyleUrl
import com.pushpal.jetlime.ItemsList
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun TrainDetailsScreen(
    apiKey: String,
    trainId: String,
    modifier: Modifier = Modifier
) {
    val announcementsKeys = remember { mutableStateListOf<String>() } // so its sorted
    val announcements = remember { mutableStateMapOf<String, LocationDetails>() }

    val mainViewModel = LocalMainViewModel.current
    val navController = LocalNavController.current

    BackHandler {
        mainViewModel.trainPositionClient.cancel()
        navController.popBackStack()
    }

    var speed by rememberSaveable { mutableIntStateOf(-1) }
    var bearing by rememberSaveable { mutableIntStateOf(-1) }
    var speedIsEstimate by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var showingMap by remember { mutableStateOf(false) }

    var currentMarker: Marker? by remember { mutableStateOf(null) }
    val icon = remember {
        IconFactory.getInstance(context)
            .fromBitmap(
                tintDrawable(
                    context = context,
                    drawableId = R.drawable.train_filled_48px,
                    color = Color.Red.toArgb()
                )
            )
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(trainId, announcementsKeys.lastOrNull()) {
        if (mainViewModel.trainPositionClient.getCurrentTrainId() == trainId || announcements.isEmpty()) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            mainViewModel.trainPositionClient.getStreamingInfo(
                trainId = trainId
            ) { info ->
                speed =
                    if (announcements[announcementsKeys.lastOrNull()]?.passed == true) 0 else info.speed

                speedIsEstimate = info.speedIsEstimate
                bearing = info.bearing

                if (info.coords != null && map != null) {
                    coroutineScope.launch(Dispatchers.Main.immediate) {
                        val position = LatLng(info.coords.latitude, info.coords.longitude)

                        val markerOptions = MarkerOptions()
                            .position(position)
                            .title("Current train location")
                            .snippet("Speed: ${speed}km/h")
                            .icon(icon)

                        if (currentMarker == null) {
                            currentMarker = map!!.addMarker(markerOptions)
                        } else {
                            currentMarker?.position = position
                            currentMarker?.snippet = "Speed: ${speed}km/h"
                        }

                        map!!.animateCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(position, 14.0)
                        )
                    }
                }
            }
        }
    }

    var mapHeight by remember { mutableIntStateOf(0) }
    var mapWidth by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopBar(
                trainId = trainId,
                productInfo = announcements.values.flatMap {
                    it.productInfo
                }.distinct(),
                speed = speed,
                speedIsEstimate = speedIsEstimate,
                bearing = bearing,
                showingMap = showingMap,
                showMap = {
                    showingMap = it

                    mapHeight = if (showingMap) (mapWidth / (16f / 9f)).toInt() else 0
                }
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

        LaunchedEffect(announcements.isNotEmpty()) {
            if (announcements.isNotEmpty()) {
                announcements.values.maxByOrNull { it.timeAtLocation ?: "" }?.let { passed ->
                    listState.animateScrollToItem(
                        index = announcements.values.indexOf(passed)
                    )
                }
            }
        }

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
            val density = LocalDensity.current
            val animatedMapHeight by animateDpAsState(
                targetValue = with(density) { mapHeight.toDp() }
            )

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
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .padding(top = animatedMapHeight)
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

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()

                        drawRoundRect(
                            color = Color.White,
                            topLeft = with(density) {
                                Offset(
                                    x = 16.dp.toPx(),
                                    y = animatedMapHeight.toPx()
                                )
                            },
                            size = with(density) {
                                 Size(
                                     width = size.width - 32.dp.toPx(),
                                     height = size.height + 32.dp.toPx() // make it taller so the bottom isn't rounded
                                 )
                            },
                            cornerRadius = with(density) {
                                CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            },
                            blendMode = BlendMode.DstOut,
                        )
                    }
                    .background(MaterialTheme.colorScheme.background)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .height(animatedMapHeight)
                    .padding(16.dp)
                    .onGloballyPositioned {
                        mapWidth = it.size.width
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(RoundedCornerConstants.ROUNDING_LARGE))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    AndroidView(
                        factory = { context ->
                            MapView(context).apply {
                                onCreate(null)
                                getMapAsync { mapLibreMap ->
                                    mapLibreMap.setStyle(MapStyleUrl)
                                    map = mapLibreMap

                                    mapLibreMap.addOnMapClickListener {
                                        mainViewModel.trainPositionClient.cancel()
                                        navController.navigate(
                                            Screens.Map(
                                                trainId = trainId,
                                                productInfo = announcements.values.flatMap {
                                                    it.productInfo
                                                }.distinct()
                                            )
                                        )
                                        false
                                    }
                                }
                            }
                        },
                        onRelease = {
                            it.onDestroy()
                        }
                    )

                    var showingShimmer by remember { mutableStateOf(true) }
                    LaunchedEffect(mapHeight) {
                        if (mapHeight > 0) showingShimmer = true

                        delay(1000)
                        showingShimmer = false
                    }

                    AnimatedVisibility(
                        visible = showingShimmer,
                        enter = fadeIn(animationSpec = tween(durationMillis = 0)),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmerEffect(
                                    durationMillis = 800
                                )
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
    productInfo: List<Information>,
    speed: Int,
    speedIsEstimate: Boolean,
    bearing: Int,
    showingMap: Boolean,
    modifier: Modifier = Modifier,
    showMap: (Boolean) -> Unit
) {
    val navController = LocalNavController.current
    val mainViewModel = LocalMainViewModel.current

    val animatedSpeed by animateIntAsState(
        targetValue = speed,
        animationSpec = tween(
            durationMillis = 800
        )
    )


    val animatedBearing by animateIntAsState(
        targetValue = bearing,
        animationSpec = tween(
            durationMillis = 800
        )
    )

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    mainViewModel.trainPositionClient.cancel()
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
                            val speedText = animatedSpeed.toString() + "km/h" + if (speedIsEstimate) "*" else ""

                            if ((desc + speedText).length >= 15) {
                                "$desc\n$speedText"
                            } else {
                                "$desc | $speedText"
                            }
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
                        .rotate(animatedBearing.toFloat())
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.compass),
                        tint = TopAppBarDefaults.topAppBarColors().titleContentColor,
                        contentDescription = "Start settings"
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.needle_tip),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Start settings"
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconToggleButton(
                checked = showingMap,
                onCheckedChange = {
                    showMap(it)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.map),
                    contentDescription = "Start settings"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        },
        modifier = modifier
    )
}