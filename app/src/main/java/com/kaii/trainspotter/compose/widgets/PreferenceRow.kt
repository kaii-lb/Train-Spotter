package com.kaii.trainspotter.compose.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants

@Composable
fun PreferencesSeparatorText(
    text: String,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontSize = TextStylingConstants.SIZE_MEDIUM,
        color = MaterialTheme.colorScheme.primary,
        textAlign = align,
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth()
    )
}

@Composable
fun PreferenceRow(
    title: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = TextStylingConstants.SIZE_MEDIUM,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(shape = RoundedCornerShape(RoundedCornerConstants.ROUNDING_LARGE))
            .background(containerColor)
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Settings row icon",
            tint = contentColor,
            modifier = Modifier
                .size(size = 28.dp)
        )

        Spacer(modifier = Modifier.width(width = 16.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            content()
        }
    }
}

@Composable
fun ApiKeyPreferenceRow(
    initialKey: ApiKey,
    isRealtimeKey: Boolean,
    setKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PreferenceRow(
        title = stringResource(
            id =
                if (isRealtimeKey) R.string.login_api_key_realtime
                else R.string.login_api_key_trafikverket
        ),
        icon = R.drawable.key,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        var placeholderKey by remember(initialKey) {
            mutableStateOf(
                if (isRealtimeKey) (initialKey as? ApiKey.Available)?.realtimeKey ?: ""
                else (initialKey as? ApiKey.Available)?.trafikVerketKey ?: ""
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            TextField(
                value = placeholderKey,
                onValueChange = { new ->
                    placeholderKey = new
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_api_key_not_available)
                    )
                },
                singleLine = true,
                isError = placeholderKey.isBlank(),
                shape = RoundedCornerShape(
                    topStart = RoundedCornerConstants.ROUNDING_MEDIUM,
                    bottomStart = RoundedCornerConstants.ROUNDING_MEDIUM
                ),
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .height(64.dp)
                    .weight(1f)
            )

            Box(
                modifier = Modifier
                    .height(64.dp)
                    .wrapContentWidth()
                    .clip(
                        RoundedCornerShape(
                            topEnd = RoundedCornerConstants.ROUNDING_MEDIUM,
                            bottomEnd = RoundedCornerConstants.ROUNDING_MEDIUM
                        )
                    )
                    .background(TextFieldDefaults.colors().containerColor(enabled = true, isError = false, focused = true))
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Confirm this API key",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            setKey(placeholderKey)
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun TextPreferencesRow(
    title: String,
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    clearBackground: Boolean = false,
    onClick: () -> Unit
) {
    PreferenceRow(
        title = title,
        icon = icon,
        fontSize = TextStylingConstants.SIZE_LARGE,
        containerColor = if (clearBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .clip(RoundedCornerShape(RoundedCornerConstants.ROUNDING_EXTRA_LARGE))
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = text,
            fontSize = TextStylingConstants.SIZE_MEDIUM,
            textAlign = TextAlign.Start,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}