package com.kaii.trafikverkettracker.compose.widgets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.api.Alert
import com.kaii.trafikverkettracker.helpers.TextStylingConstants

@Composable
fun TrafikAlertDialog(
    alerts: List<Alert>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(1f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
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
}

@Composable
fun FullWidthDialogButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(1f)
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary)
            .clickable {
                onClick()
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = TextStylingConstants.SIZE_SMALL
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}