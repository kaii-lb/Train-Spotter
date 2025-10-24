package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R
import com.kaii.trainspotter.api.Alert
import com.kaii.trainspotter.helpers.TextStylingConstants

@Composable
fun TrafikAlertDialog(
    alerts: List<Alert>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogBase(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.timetable_alert),
            fontSize = TextStylingConstants.SIZE_LARGE,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.Top
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = alerts.size,
                key = { index ->
                    alerts[index].title + alerts[index].text
                }
            ) { index ->
                val alert = alerts[index]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = alert.title,
                            fontSize = TextStylingConstants.SIZE_MEDIUM,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        if (alert.type.isNotBlank()) {
                            Spacer(modifier = Modifier.height(height = 2.dp))

                            Text(
                                text = alert.type,
                                fontSize = TextStylingConstants.SIZE_EXTRA_SMALL,
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(height = 8.dp))

                        Text(
                            text = alert.text,
                            fontSize = TextStylingConstants.SIZE_SMALL,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FullWidthDialogButton(
            text = stringResource(id = R.string.okay)
        ) {
            onDismiss()
        }
    }
}