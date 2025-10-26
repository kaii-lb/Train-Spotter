package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.api.LocationDetails
import com.kaii.trainspotter.helpers.TextStylingConstants
import com.pushpal.jetlime.EventPointType
import com.pushpal.jetlime.EventPosition
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrainDetailTableElement(
    item: LocationDetails,
    position: EventPosition,
    modifier: Modifier = Modifier
) {
    val departure = remember { item.departureTimeFormatted }
    val arrival = remember { item.arrivalTimeFormatted }
    val estDeparture = remember { item.estimatedDepartureTimeFormatted }
    val estArrival = remember { item.estimatedArrivalTimeFormatted }
    val stopsAtLocation = remember {
        arrival != departure
                && arrival.isNotBlank() && departure.isNotBlank()
                && estArrival != estDeparture
    }

    JetLimeExtendedEvent(
        style = JetLimeEventDefaults.eventStyle(
            position = position,
            pointType =
                if (item.passed) EventPointType.Default
                else EventPointType.EMPTY
        ),
        additionalContentMaxWidth = 88.dp,
        modifier = Modifier.heightIn(
            min = if (stopsAtLocation) 96.dp
            else 80.dp
        ),
        additionalContent = {
            var showInfoDialog by remember { mutableStateOf(false) }

            if (showInfoDialog) {
                TimeInfoDialog(
                    item = item,
                    onDismiss = {
                        showInfoDialog = false
                    }
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (item.delay.isNotBlank()) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .clickable {
                        showInfoDialog = true
                    }
            ) {
                if (stopsAtLocation) {
                    Column(
                        modifier = Modifier
                            .heightIn(min = 80.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estArrival ?: arrival,
                            modifier = Modifier
                                .fillMaxWidth(),
                            fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "->",
                            modifier = Modifier
                                .rotate(90f)
                                .wrapContentSize(),
                            fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = estDeparture ?: departure,
                            modifier = Modifier
                                .fillMaxWidth(),
                            fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = when {
                            estDeparture != null -> estDeparture
                            estArrival != null -> estArrival
                            departure.isNotBlank() -> departure
                            else -> arrival
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                        textAlign = TextAlign.Center
                    )
                }
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
                        text = item.name,
                        fontSize = TextStylingConstants.SIZE_MEDIUM,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.track.isNotBlank()) {
                        Text(
                            text = "Track: ${item.track}",
                            fontSize = TextStylingConstants.SIZE_MEDIUM
                        )
                    }

                    if (item.delay.isNotBlank()) {
                        val delayString = stringResource(id = R.string.timetable_delay, item.delay).split(" ")
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
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (item.canceled
                        || item.deviations.any {
                            it.description.lowercase().contains("inställt")
                                    || it.description.lowercase().contains("inställd")
                        }
                    ) {
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

                    if (item.deviations.isNotEmpty()) {
                        var showDialog by remember { mutableStateOf(false) }

                        if (showDialog) {
                            TrafikAlertDialog(
                                alerts = item.deviations.map {
                                    Alert(
                                        type = "",
                                        title = it.code,
                                        text = it.description
                                    )
                                },
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