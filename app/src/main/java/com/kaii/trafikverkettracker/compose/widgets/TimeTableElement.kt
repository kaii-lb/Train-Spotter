package com.kaii.trafikverkettracker.compose.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.api.TimetableEntry
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import com.kaii.trafikverkettracker.helpers.formatDelay
import com.kaii.trafikverkettracker.helpers.formatSecondsToTime
import com.pushpal.jetlime.EventPosition
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent

@OptIn(ExperimentalComposeApi::class)
@Composable
fun TimeTableElement(
    item: TimetableEntry,
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
                Text(
                    text = formatSecondsToTime(item.time),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                    textAlign = TextAlign.Center,
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
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = item.route.destination.name,
                        fontSize = TextStylingConstants.SIZE_MEDIUM,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.canceled) {
                        Text(
                            text = "Canceled",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer)
                        )
                    }

                    if (item.delay != 0) {
                        val delay = formatDelay(item.delay)

                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(color = MaterialTheme.colorScheme.primary)
                                ) {
                                    append("Delay: ")
                                }

                                append(delay)
                            },
                        )
                    }

                    Text(
                        text = "Via: ${item.route.transportMode}",
                        fontSize = TextStylingConstants.SIZE_MEDIUM
                    )
                }

                if (item.alerts.isNotEmpty()) {
                    var showDialog by remember { mutableStateOf(false) }

                    if (showDialog) {
                        TrafikAlertDialog(
                            alerts = item.alerts,
                            onDismiss = {
                                showDialog = false
                            }
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.warning),
                        contentDescription = "Alert!",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                showDialog = true
                            }
                    )
                }
            }
        }
    }
}