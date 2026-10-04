package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kaii.trainspotter.LocalNavController
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.api.Stop
import com.kaii.trainspotter.domain.station.Route
import com.kaii.trainspotter.domain.station.TimetableEntry
import com.kaii.trainspotter.domain.station.TransportMode
import com.kaii.trainspotter.helpers.Screens
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.kaii.trainspotter.helpers.formatDelay
import com.kaii.trainspotter.helpers.formatSecondsToTime
import com.kaii.trainspotter.ui.theme.LocalExtraColorsPalette
import com.pushpal.jetlime.EventPosition
import com.pushpal.jetlime.JetLimeDefaults
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent
import com.pushpal.jetlime.LocalJetLimeStyle
import kotlinx.coroutines.launch

@Preview
@Composable
private fun TimeTableElementPreview() {
    CompositionLocalProvider(
        LocalJetLimeStyle provides JetLimeDefaults.columnStyle()
    ) {
        TimeTableElement(
            item = TimetableEntry(
                scheduled = "2026-10-02T01:05:00.000",
                realtime = "",
                delay = 0,
                canceled = false,
                route = Route(
                    name = "Route",
                    designation = "",
                    transportModeCode = 0,
                    transportMode = TransportMode.Train,
                    direction = "",
                    origin = Stop(
                        id = "1",
                        name = "Stop1"
                    ),
                    destination = Stop(
                        id = "2",
                        name = "Stop2"
                    )
                ),
                trip = null,
                agency = null,
                stop = null,
                isRealtime = false,
                alerts = listOf(
                    Alert(
                        type = "Alert",
                        title = "Bad thing",
                        text = "This is not supposed to happened",
                        isDeviation = false
                    ),
                    Alert(
                        type = "Cancelled",
                        title = "inställt",
                        text = "This is really not supposed to happened",
                        isDeviation = true
                    )
                )
            ),
            position = EventPosition.dynamic(0, 4),
            navController = rememberNavController()
        )
    }
}

@OptIn(ExperimentalComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimeTableElement(
    item: TimetableEntry,
    position: EventPosition,
    modifier: Modifier = Modifier,
    navController: NavController = LocalNavController.current
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
                        else if (item.delay < 0) LocalExtraColorsPalette.current.success
                        else MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Text(
                    text = formatSecondsToTime(item.time),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                    textAlign = TextAlign.Center,
                    color =
                        if (item.delay > 0) MaterialTheme.colorScheme.onErrorContainer
                        else if (item.delay < 0) LocalExtraColorsPalette.current.onSuccess
                        else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            modifier = modifier
                .wrapContentHeight()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(CardDefaults.shape)
                    .clickable(
                        enabled = item.route?.transportMode == TransportMode.Train ||
                                item.route?.transportMode == TransportMode.Metro
                    ) {
                        navController.navigate(
                            route = Screens.TrainDetails(
                                trainId = item.trip?.technicalNumber.toString()
                            )
                        )
                    }
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
                            text = item.route?.destination?.name ?: "",
                            fontSize = TextStylingConstants.SIZE_MEDIUM,
                            fontWeight = FontWeight.Bold
                        )

                        if (item.delay != 0) {
                            val delay = formatDelay(item.delay)

                            val delayString = stringResource(
                                id = if (item.delay > 0) R.string.timetable_delay else R.string.timetable_advance,
                                delay
                            ).split(" ")

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
                            text = "Via: ${item.route?.transportMode}",
                            fontSize = TextStylingConstants.SIZE_MEDIUM
                        )
                    }

                    val hasWarnings = item.alerts.isNotEmpty()
                    val isCancelled = remember(item.canceled, item.alerts) {
                        item.canceled || item.alerts.any {
                            it.title.lowercase().contains("inställt")
                                    || it.title.lowercase().contains("inställd")
                        }
                    }

                    if (hasWarnings || isCancelled) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isCancelled) {
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
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
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

                        if (hasWarnings) {
                            var showDialog by remember { mutableStateOf(false) }

                            if (showDialog) {
                                AlertBottomSheet(
                                    alerts = item.alerts,
                                    onDismiss = {
                                        showDialog = false
                                    }
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    showDialog = true
                                },
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.warning),
                                    contentDescription = stringResource(id = R.string.timetable_alert),
                                    modifier = Modifier.offset(y = (-1).dp)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}