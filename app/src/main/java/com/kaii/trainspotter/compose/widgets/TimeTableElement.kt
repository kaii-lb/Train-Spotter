package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.TimetableEntry
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.helpers.formatDelay
import com.kaii.trainspotter.helpers.formatSecondsToTime
import com.pushpal.jetlime.EventPosition
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeApi::class, ExperimentalMaterial3Api::class)
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
            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (item.delay > 0) MaterialTheme.colorScheme.errorContainer
                        else if (item.delay < 0) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                ),
            ) {
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

                    if (item.delay != 0) {
                        val delay = formatDelay(item.delay)

                        val delayString = stringResource(id = R.string.timetable_delay, delay).split(" ")
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(color = MaterialTheme.colorScheme.primary)
                                ) {
                                    append(delayString[0] + " ")
                                }

                                append(delayString[1])
                            },
                        )
                    }

                    Text(
                        text = "Via: ${item.route.transportMode}",
                        fontSize = TextStylingConstants.SIZE_MEDIUM
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (item.canceled || item.alerts.any { it.title.lowercase().contains("inställd") }) {
                        val tooltipState = rememberTooltipState(isPersistent = true)
                        val coroutineScope = rememberCoroutineScope()

                        TooltipBox(
                            state = tooltipState,
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Start, 8.dp),
                            tooltip = {
                                PlainTooltip(
                                    caretShape = TooltipDefaults.caretShape(),
                                    content = {
                                        Text(
                                            text = stringResource(id = R.string.timetable_train_canceled)
                                        )
                                    }
                                )
                            }
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (tooltipState.isVisible) tooltipState.dismiss()
                                        else tooltipState.show()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.dangerous),
                                    contentDescription = stringResource(id = R.string.timetable_train_canceled),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
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

                        IconButton(
                            onClick = {
                                showDialog = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.warning),
                                contentDescription = stringResource(id = R.string.timetable_alert),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}