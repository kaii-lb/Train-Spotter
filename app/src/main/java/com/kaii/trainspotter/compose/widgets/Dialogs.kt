package com.kaii.trainspotter.compose.widgets

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Information
import com.kaii.trainspotter.api.LocationDetails
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.helpers.TextStylingConstants

@Composable
fun DialogBase(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(shape = RoundedCornerShape(RoundedCornerConstants.ROUNDING_EXTRA_LARGE))
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun FullWidthDialogButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(1f)
            .height(48.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable {
                onClick()
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = TextStylingConstants.SIZE_SMALL
        )
    }
}

@Composable
fun ApiKeyExplanationDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    DialogBase(
        onDismiss = onDismiss,
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.login_api_key_get),
            fontSize = TextStylingConstants.SIZE_LARGE,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = AnnotatedString.fromHtml(
                htmlString = stringResource(id = R.string.login_api_key_get_desc, MaterialTheme.colorScheme.primary.toArgb())
            ),
            fontSize = TextStylingConstants.SIZE_MEDIUM,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .wrapContentSize()
        )

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        FullWidthDialogButton(
            text = stringResource(id = R.string.login_api_key_open_link)
        ) {
            val intent = Intent().apply {
                data = "https://developer.trafiklab.se".toUri()
                action = Intent.ACTION_VIEW
            }

            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(8.dp))

        FullWidthDialogButton(
            text = stringResource(id = R.string.close),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = onDismiss
        )
    }
}

@Composable
fun TimeInfoDialog(
    item: LocationDetails,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    DialogBase(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.timetable_time_info),
                fontSize = TextStylingConstants.SIZE_LARGE,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.timetable_time_info_arrival_advertised, item.arrivalTimeFormatted),
                fontSize = TextStylingConstants.SIZE_MEDIUM,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(
                    id = R.string.timetable_time_info_arrival_actual,
                    item.estimatedArrivalTimeFormatted ?: item.arrivalTimeFormatted
                ),
                fontSize = TextStylingConstants.SIZE_MEDIUM,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(id = R.string.timetable_time_info_departure_advertised, item.departureTimeFormatted),
                fontSize = TextStylingConstants.SIZE_MEDIUM,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(
                    id = R.string.timetable_time_info_departure_actual,
                    item.estimatedDepartureTimeFormatted ?: item.departureTimeFormatted
                ),
                fontSize = TextStylingConstants.SIZE_MEDIUM,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FullWidthDialogButton(
            text = stringResource(id = R.string.close),
            onClick = onDismiss
        )
    }
}

@Composable
fun TrainInfoDialog(
    info: List<Information>,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    DialogBase(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.train_info),
            fontSize = TextStylingConstants.SIZE_LARGE,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = info
            ) { item ->
                Text(
                    text = stringResource(id = item.code.toInt(), item.description),
                    fontSize = TextStylingConstants.SIZE_MEDIUM,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FullWidthDialogButton(
            text = stringResource(id = R.string.close),
            onClick = onDismiss
        )
    }
}