package com.kaii.trainspotter.compose.widgets

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.pushpal.jetlime.EventPosition
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent
import kotlin.random.Random

@Composable
fun Modifier.shimmerEffect(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    highlightColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    durationMillis: Int = 2000,
    delayMillis: Int = 0
) = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition()
    val startOffset by transition.animateFloat(
        initialValue = -3f * size.width,
        targetValue = 3f * size.width,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis
            )
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                containerColor,
                highlightColor,
                containerColor
            ),
            start = Offset(startOffset, 0f),
            end = Offset(startOffset + size.width, size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun SearchShimmerLoadingItem(
    position: SearchItemPositon,
    modifier: Modifier = Modifier
) {
    val shape = when (position) {
        SearchItemPositon.Single -> RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_MEDIUM)

        SearchItemPositon.Top -> RoundedCornerShape(
            topStart = RoundedCornerConstants.ROUNDING_MEDIUM,
            topEnd = RoundedCornerConstants.ROUNDING_MEDIUM
        )

        SearchItemPositon.Middle -> RoundedCornerShape(size = 0.dp)

        SearchItemPositon.Bottom -> RoundedCornerShape(
            bottomStart = RoundedCornerConstants.ROUNDING_MEDIUM,
            bottomEnd = RoundedCornerConstants.ROUNDING_MEDIUM
        )
    }

    val separatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .drawBehind {
                val strokeWidth = 0.5f.dp.toPx() * density
                val y = size.height - strokeWidth / 2

                if (position != SearchItemPositon.Bottom && position != SearchItemPositon.Single) {
                    drawLine(
                        color = separatorColor,
                        start = Offset(8.dp.toPx(), y),
                        end = Offset(size.width - 8.dp.toPx(), y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(Random.nextFloat() * 0.75f + 0.25f)
                .height(24.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(Random.nextFloat() * 0.5f + 0.5f)
                .height(18.dp)
                .clip(CircleShape)
                .shimmerEffect(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                    highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                )
        )
    }
}

@OptIn(ExperimentalComposeApi::class)
@Composable
fun TableShimmerLoadingElement(
    position: EventPosition,
    modifier: Modifier = Modifier
) {
    JetLimeExtendedEvent(
        style = JetLimeEventDefaults.eventStyle(
            position = position
        ),
        additionalContentMaxWidth = 88.dp,
        additionalContent = {
            Card {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(88.dp)
                        .padding(all = 12.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(all = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(Random.nextFloat() * 0.75f + 0.25f)
                            .height(24.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(Random.nextFloat() * 0.5f + 0.5f)
                            .height(18.dp)
                            .clip(CircleShape)
                            .shimmerEffect(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                                highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            )
                    )
                }
            }
        }
    }
}