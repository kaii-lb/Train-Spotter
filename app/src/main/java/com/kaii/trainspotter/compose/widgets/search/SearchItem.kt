package com.kaii.trainspotter.compose.widgets.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.domain.TransportMode
import com.kaii.trainspotter.presentation.getSearchItemShapeFromPosition
import com.kaii.trainspotter.presentation.icon

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
    hasError: Boolean,
    position: SearchItemPositon,
    transportMode: TransportMode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = getSearchItemShapeFromPosition(position)

    FilledTonalButton(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(
            shape = shape,
            pressedShape = CircleShape
        ),
        contentPadding = PaddingValues(
            top = 12.dp, start = 12.dp,
            bottom = 12.dp, end = 16.dp
        ),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.Start
            )
        ) {
            Icon(
                painter = painterResource(id = transportMode.icon),
                contentDescription = transportMode.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = name,
                    textAlign = TextAlign.Start,
                    // fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )

                Text(
                    text = description,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            if (hasError) {
                Icon(
                    painter = painterResource(id = R.drawable.warning),
                    contentDescription = "Alert!",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}