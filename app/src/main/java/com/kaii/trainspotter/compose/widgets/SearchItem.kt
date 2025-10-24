package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.StopGroup
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants

enum class SearchItemPositon {
    Top,
    Middle,
    Bottom,
    Single
}

@Composable
fun SearchItem(
    name: String,
    description: String,
    modifier: Modifier = Modifier,
    position: SearchItemPositon,
    errorContent: @Composable RowScope.() -> Unit = {},
    onClick: () -> Unit
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {
                onClick()
            }
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = name,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = TextStylingConstants.SIZE_MEDIUM,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                textAlign = TextAlign.Start,
                fontSize = TextStylingConstants.SIZE_SMALL,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }

        errorContent()
    }
}

@Composable
fun StopSearchItem(
    stop: StopGroup,
    position: SearchItemPositon,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SearchItem(
        name = stop.name,
        description = stringResource(
            id = R.string.search_transport_modes,
            stop.transportModes.joinToString {
                it.name
            }
        ),
        position = position,
        modifier = modifier,
        errorContent = {
            if (stop.stops.any { it.alerts.isNotEmpty() }) {
                Icon(
                    painter = painterResource(id = R.drawable.warning),
                    contentDescription = "Alert!",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        onClick = onClick
    )
}