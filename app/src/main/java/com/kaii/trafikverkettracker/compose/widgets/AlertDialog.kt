package com.kaii.trafikverkettracker.compose.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.api.Alert
import com.kaii.trafikverkettracker.helpers.TextStylingConstants

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
            text = stringResource(id = R.string.alert),
            fontSize = TextStylingConstants.SIZE_LARGE,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.wrapContentSize()
        )

        Spacer(modifier = Modifier.height(24.dp))

        alerts.forEach { alert ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = alert.title + ": " + alert.type,
                    fontSize = TextStylingConstants.SIZE_MEDIUM,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .wrapContentSize()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = alert.text,
                    fontSize = TextStylingConstants.SIZE_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .wrapContentSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FullWidthDialogButton(
            text = stringResource(id = R.string.okay)
        ) {
            onDismiss()
        }
    }
}