@file:Suppress("deprecation")

package com.kaii.trainspotter.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaii.trainspotter.LocalMainViewModel
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Information
import com.kaii.trainspotter.api.TrainInformation
import com.kaii.trainspotter.compose.widgets.TrainInfoDialog
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.helpers.tintDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapScreen(
    trainId: String,
    productInfo: List<Information>,
    modifier: Modifier = Modifier
) {
    val mainViewModel = LocalMainViewModel.current
    BackHandler {
        mainViewModel.trainPositionClient.cancel()
    }

    val mapStyle = """
    {
      "version": 8,
      "sources": {
        "osm": {
          "type": "raster",
          "tiles": ["https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"],
          "tileSize": 256,
          "attribution": "&copy; OpenStreetMap Contributors"
        }
      },
      "layers": [
        {
          "id": "simple-tiles",
          "type": "raster",
          "source": "osm",
          "minzoom": 0,
          "maxzoom": 22
        }
      ]
    }
    """.trimIndent()

    var speedIsEstimate by remember { mutableStateOf(false) }
    var speed by rememberSaveable { mutableIntStateOf(-1) }
    var bearing by rememberSaveable { mutableIntStateOf(-1) }

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(trainId) {
        if (mainViewModel.trainPositionClient.getCurrentTrainId() == trainId) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            var currentMarker: Marker? = null

            mainViewModel.trainPositionClient.getStreamingInfo(
                trainId = trainId
            ) { info ->
                speed = if (info.speed == null && info.position != null && info.timeStamp != null) {
                    speedIsEstimate = true

                    info.position.toCoords(info.timeStamp)?.let { current ->
                        val new = mainViewModel.trainPositionClient.calcSpeed(
                            coords = current
                        )

                        new
                    } ?: 0
                } else {
                    speedIsEstimate = false
                    info.speed ?: -1
                }

                bearing = info.bearing ?: -1

                if (info.position != null && info.timeStamp != null) {
                    info.position.toCoords(info.timeStamp)?.let { current ->
                        val position = LatLng(current.latitude, current.longitude)

                        val markerOptions = MarkerOptions()
                            .position(position)
                            .title("Current train location")
                            .snippet("Speed: ${speed}km/h")
                            .icon(
                                IconFactory.getInstance(context)
                                    .fromBitmap(
                                        tintDrawable(
                                            context = context,
                                            drawableId = R.drawable.train_filled_48px,
                                            color = Color.Red.toArgb()
                                        )
                                    )
                            )

                        coroutineScope.launch(Dispatchers.Main) {
                            if (currentMarker == null) {
                                currentMarker = map?.addMarker(markerOptions)
                                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12.0))
                            } else {
                                currentMarker?.position = position
                                currentMarker?.snippet = "Speed: ${speed}km/h"
                                map?.animateCamera(CameraUpdateFactory.newLatLng(position))
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                trainId = trainId,
                productInfo = productInfo,
                speed = speed,
                bearing = bearing,
                speedIsEstimate = speedIsEstimate
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        getMapAsync {
                            it.setStyle(
                                Style.Builder()
                                    .fromJson(mapStyle)
                            )
                            map = it
                        }
                    }
                },
                onRelease = {
                    it.onDestroy()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    trainId: String,
    productInfo: List<Information>,
    speed: Int,
    bearing: Int,
    speedIsEstimate: Boolean,
    modifier: Modifier = Modifier
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
                            if (desc.length >= 15) {
                                desc + "\nSpeed: " + animatedSpeed.toString() + "km/h" + if (speedIsEstimate) "*" else ""
                            } else {
                                desc + " | " + animatedSpeed.toString() + "km/h" + if (speedIsEstimate) "*" else ""
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
        },
        modifier = modifier
    )
}