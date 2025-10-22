package com.kaii.trafikverkettracker.compose.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.api.LocationDetails
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import com.pushpal.jetlime.EventPosition
import com.pushpal.jetlime.JetLimeEventDefaults
import com.pushpal.jetlime.JetLimeExtendedEvent

@OptIn(ExperimentalComposeApi::class)
@Composable
fun TrainDetailTableElement(
    item: LocationDetails,
    position: EventPosition,
    modifier: Modifier = Modifier
) {
    val departure = remember { item.departureTimeFormatted }
    val arrival = remember { item.arrivalTimeFormatted }

    JetLimeExtendedEvent(
        style = JetLimeEventDefaults.eventStyle(
            position = position
        ),
        additionalContentMaxWidth = 88.dp,
        modifier = Modifier.height(
            if (arrival != departure && arrival.isNotBlank() && departure.isNotBlank()) 96.dp
            else 80.dp
        ),
        additionalContent = {
            Card {
                if (arrival != departure && arrival.isNotBlank() && departure.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = arrival,
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
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = departure,
                            modifier = Modifier
                                .fillMaxWidth(),
                            fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = departure.ifBlank { arrival },
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

                    Text(
                        text = "Track: ${item.track}",
                        fontSize = TextStylingConstants.SIZE_MEDIUM
                    )
                }
            }
        }
    }
}