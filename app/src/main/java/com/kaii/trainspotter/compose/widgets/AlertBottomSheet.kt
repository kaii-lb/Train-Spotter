package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.helpers.RoundedCornerConstants

@Preview
@Composable
private fun AlertBottomSheetPreview() {
    Box(
        modifier = Modifier
            .height(1920.dp)
            .width(1080.dp)
    ) {
        AlertBottomSheet(
            alerts = listOf(
                Alert(
                    type = "Alert",
                    title = "Bad thing",
                    text = "This is not supposed to happened",
                    isDeviation = false
                ),
                Alert(
                    type = "Deviation - Cancelled",
                    title = "inställt",
                    text = "This is really not supposed to happened",
                    isDeviation = true
                )
            ),
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertBottomSheet(
    alerts: List<Alert>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides null
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            CompositionLocalProvider(
                LocalRippleConfiguration provides RippleConfiguration()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                    modifier = Modifier
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.timetable_alerts),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(
                            space = 12.dp,
                            alignment = Alignment.Top
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(
                            items = alerts,
                            key = { _, item ->
                                item.title + item.text + item.type
                            }
                        ) { index, alert ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(all = 12.dp)
                                ) {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.bodyLargeEmphasized,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Start,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    if (alert.isDeviation) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.railway_alert_2),
                                            contentDescription = stringResource(id = R.string.train_info_alert_is_deviation)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_MEDIUM))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(all = 8.dp),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (alert.type.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(height = 2.dp))

                                        Text(
                                            text = alert.type,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Start,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(height = 8.dp))

                                    Text(
                                        text = alert.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Start,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }

                                if (alerts.size != 1 && index != alerts.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .height(2.dp)
                                            .fillMaxWidth(0.8f)
                                            .clip(CircleShape)
                                            .background(
                                                color =
                                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceBright
                                                    else MaterialTheme.colorScheme.surfaceDim
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}