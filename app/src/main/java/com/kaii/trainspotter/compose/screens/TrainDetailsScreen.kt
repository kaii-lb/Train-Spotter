@file:Suppress("deprecation")

package com.kaii.trainspotter.compose.screens

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.compose.widgets.TableShimmerLoadingElement
import com.kaii.trainspotter.compose.widgets.TrainDetailTableElement
import com.kaii.trainspotter.compose.widgets.TrainInfoDialog
import com.kaii.trainspotter.compose.widgets.shimmerEffect
import com.kaii.trainspotter.data.TrainUpdateService
import com.kaii.trainspotter.domain.station.Information
import com.kaii.trainspotter.domain.train.TrainInformation
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.SpeedPointDisplay
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.helpers.createTrainIcon
import com.kaii.trainspotter.models.TrainDetailsMapState
import com.kaii.trainspotter.models.TrainDetailsViewModel
import com.kaii.trainspotter.ui.theme.MapStyleJson
import com.pushpal.jetlime.JetLimeColumn
import com.pushpal.jetlime.JetLimeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val TrainDetailsMapState.Loaded.latLng: LatLng
    get() = LatLng(latitude, longitude)

@Composable
fun TrainDetailsScreen(
    trainId: String,
    viewModel: TrainDetailsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val listState = rememberLazyListState()

    var notificationRequested by rememberSaveable(trainId) { mutableStateOf(false) }

    LaunchedEffect(trainId) {
        viewModel.track(trainId)

        if (!notificationRequested) {
            notificationRequested = true
            TrainUpdateService.start(context)
        }
    }

    LaunchedEffect(viewModel, listState) {
        viewModel.scrollEvents.collect { index ->
            snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it > index }
            listState.animateScrollToItem(index = index)
        }
    }

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapStyle by remember { mutableStateOf<Style?>(null) }
    var showingMap by remember { mutableStateOf(false) }
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()

    var mapHeight by remember { mutableIntStateOf(0) }
    var mapWidth by remember { mutableIntStateOf(0) }
    var maxHeight by remember { mutableIntStateOf(0) }
    var currentMarker: Marker? by remember { mutableStateOf(null) }

    val mapView = remember(context) { MapView(context).apply { onCreate(null) } }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)

            map = null
            mapStyle = null
            currentMarker = null

            val state = lifecycleOwner.lifecycle.currentState
            if (state.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (state.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { mapLibreMap ->
            map = mapLibreMap

            mapLibreMap.addOnMapClickListener {
                mapHeight = if (mapHeight == maxHeight) {
                    (mapWidth / (16f / 9f)).toInt()
                } else {
                    maxHeight
                }

                false
            }

            mapLibreMap.setStyle(
                Style.Builder().fromJson(MapStyleJson)
            ) { style ->
                if (style.getSource("speed-source") == null) {
                    setupSpeedLayer(context, style)
                }
                mapStyle = style
            }
        }
    }

    val speedPointDisplay = remember(context) { SpeedPointDisplay(context) }

    DisposableEffect(speedPointDisplay) {
        onDispose { speedPointDisplay.release() }
    }

    LaunchedEffect(map, mapStyle) {
        val currentMap = map
        if (currentMap != null && mapStyle != null) {
            speedPointDisplay.fetchSpeedsForBounds(mapLibreMap = currentMap)
        }
    }

    var cachedBearing by remember { mutableIntStateOf(0) }
    var cachedIcon by remember {
        mutableStateOf(
            IconFactory.getInstance(context)
                .fromBitmap(
                    createTrainIcon(
                        context = context,
                        bearing = null,
                        color = Color.Red.toArgb()
                    )
                )
        )
    }

    LaunchedEffect(mapState, map) {
        val state = mapState as? TrainDetailsMapState.Loaded ?: return@LaunchedEffect
        val currentMap = map ?: return@LaunchedEffect

        if (cachedBearing != state.bearing) {
            val mapIcon = withContext(Dispatchers.Default) {
                IconFactory.getInstance(context)
                    .fromBitmap(
                        createTrainIcon(
                            context = context,
                            bearing = state.bearing,
                            color = Color.Red.toArgb()
                        )
                    )
            }

            cachedBearing = state.bearing
            cachedIcon = mapIcon
        }

        if (currentMarker == null) {
            currentMarker = currentMap.addMarker(
                MarkerOptions()
                    .position(state.latLng)
                    .title("Current train location")
                    .snippet("Speed: ${state.speed}km/h")
                    .icon(cachedIcon)
            )
        } else {
            currentMarker?.position = state.latLng
            currentMarker?.snippet = "Speed: ${state.speed}km/h"
            currentMarker?.icon = cachedIcon
        }

        if (showingMap) {
            currentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(state.latLng, 14.0))
        }
    }

    LaunchedEffect(showingMap) {
        if (!showingMap) return@LaunchedEffect

        delay(500.milliseconds)

        val state = mapState as? TrainDetailsMapState.Loaded ?: return@LaunchedEffect
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(state.latLng, 14.0))
    }

    Scaffold(
        topBar = {
            val productInfo by viewModel.productInfo.collectAsStateWithLifecycle()

            TopBar(
                trainId = trainId,
                productInfo = { productInfo },
                mapState = { mapState },
                showingMap = { showingMap },
                showMap = {
                    showingMap = it
                    mapHeight = if (it) (mapWidth / (16f / 9f)).toInt() else 0
                },
                onBackClick = navController::popBackStack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.onRefresh()
            },
            modifier = Modifier
                .padding(innerPadding)
                .onGloballyPositioned {
                    maxHeight = it.size.height
                }
        ) {
            val density = LocalDensity.current
            val items by viewModel.items.collectAsStateWithLifecycle()
            val animatedMapHeight by animateDpAsState(
                targetValue = with(density) { mapHeight.toDp() }
            )

            JetLimeColumn(
                listState = listState,
                itemsList = items,
                style = JetLimeDefaults.columnStyle(
                    itemSpacing = 16.dp,
                ),
                key = { _, item -> item.signature.ifEmpty { item.name } },
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
                    } else {
                        TrainDetailTableElement(
                            item = item,
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
                                    width = size.width - 24.dp.toPx(),
                                    height = size.height - 12.dp.toPx()
                                )
                            },
                            cornerRadius = with(density) {
                                CornerRadius(
                                    x = RoundedCornerConstants.ROUNDING_LARGE.toPx(),
                                    y = RoundedCornerConstants.ROUNDING_LARGE.toPx()
                                )
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
                        factory = { mapView }
                    )

                    var showingShimmer by remember { mutableStateOf(true) }
                    LaunchedEffect(showingMap) {
                        if (showingMap) showingShimmer = true

                        delay(1.seconds)
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

private fun setupSpeedLayer(context: Context, style: Style) {
    val sourceOptions = GeoJsonOptions()
        .withCluster(true)
        .withClusterRadius(10)
        .withClusterMaxZoom(8)
        .withMinZoom(2)
        .withClusterProperty( // TODO: not working, fix
            "cluster_speed",
            Expression.min(
                Expression.accumulated(),
                Expression.get("speed")
            ),
            Expression.get("speed")
        )

    style.addSource(GeoJsonSource("speed-source", sourceOptions))

    val drawable = ContextCompat.getDrawable(context, R.drawable.circle) ?: return
    style.addImage("bubble-icon", drawable)

    val speedDisplay = Expression.coalesce(
        Expression.get("cluster_speed"),
        Expression.get("speed"),
        Expression.literal("?")
    )

    val textLayer = SymbolLayer("speed-text-layer", "speed-source")
        .withProperties(
            PropertyFactory.textField(speedDisplay),
            PropertyFactory.textFont(arrayOf("Open Sans Regular", "Arial Unicode MS Regular")),
            PropertyFactory.textSize(
                Expression.step(
                    Expression.length(speedDisplay),
                    14f,
                    Expression.stop(2, 14f),
                    Expression.stop(3, 12f)
                )
            ),
            PropertyFactory.textColor(Color.Black.toArgb()),
            PropertyFactory.textAnchor(Property.TEXT_ANCHOR_CENTER),
            PropertyFactory.textPadding(0f),

            PropertyFactory.iconImage("bubble-icon"),
            PropertyFactory.iconSize(1.3f),
            PropertyFactory.iconPadding(0f),
            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),

            PropertyFactory.textOptional(false),
            PropertyFactory.iconOptional(false),
            PropertyFactory.textAllowOverlap(true),
            PropertyFactory.iconAllowOverlap(false),
            PropertyFactory.textIgnorePlacement(true),
            PropertyFactory.iconIgnorePlacement(false)
        )

    textLayer.minZoom = 10f
    style.addLayer(textLayer)
}

@Composable
private fun TopBar(
    trainId: String,
    productInfo: () -> List<Information>,
    mapState: () -> TrainDetailsMapState,
    showingMap: () -> Boolean,
    modifier: Modifier = Modifier,
    showMap: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    val animatedSpeed by animateIntAsState(
        targetValue = (mapState() as? TrainDetailsMapState.Loaded)?.speed ?: -1,
        animationSpec = tween(durationMillis = 800)
    )

    val animatedBearing by animateIntAsState(
        targetValue = (mapState() as? TrainDetailsMapState.Loaded)?.bearing ?: -1,
        animationSpec = tween(durationMillis = 800)
    )

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Return to previous page"
                )
            }
        },
        title = {
            var showDialog by remember { mutableStateOf(false) }
            val info = remember(productInfo()) {
                productInfo().find {
                    it.code == TrainInformation.Product.type.toString()
                }
            }

            if (showDialog) {
                TrainInfoDialog(
                    info = productInfo(),
                    onDismiss = { showDialog = false }
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showDialog = true }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                val desc =
                    if (info != null) "${info.description} | $trainId"
                    else "Train: $trainId"

                val title = when (val state = mapState()) {
                    is TrainDetailsMapState.Loading -> desc
                    is TrainDetailsMapState.Loaded -> {
                        val speedText = animatedSpeed.toString() + "km/h" + if (state.speedIsEstimate) "*" else ""

                        if ((desc + speedText).length >= 15) "$desc\n$speedText"
                        else "$desc | $speedText"
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
                visible = mapState() !is TrainDetailsMapState.Loading,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = animatedBearing.toFloat()
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.compass),
                        tint = TopAppBarDefaults.topAppBarColors().titleContentColor,
                        contentDescription = "Compass",
                        modifier = Modifier
                            .size(32.dp)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.needle_tip),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null, // decorative
                        modifier = Modifier
                            .size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconToggleButton(
                checked = showingMap(),
                onCheckedChange = { showMap(it) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.map),
                    contentDescription = "Toggle map"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        },
        modifier = modifier
    )
}