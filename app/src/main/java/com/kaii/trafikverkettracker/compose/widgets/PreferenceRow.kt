package com.kaii.trafikverkettracker.compose.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaii.trafikverkettracker.LocalMainViewModel
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.datastore.ApiKey
import com.kaii.trafikverkettracker.helpers.RoundedCornerConstants
import com.kaii.trafikverkettracker.helpers.TextStylingConstants

@Composable
fun PreferencesSeparatorText(text: String) {
    Text(
        text = text,
        fontSize = TextStylingConstants.SIZE_MEDIUM,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(12.dp)
    )
}

@Composable
fun PreferenceRow(
    title: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Settings row icon",
            modifier = Modifier
                .size(size = 28.dp)
        )

        Spacer(modifier = Modifier.width(width = 16.dp))

        Column {
            Text(
                text = title,
                fontSize = TextStylingConstants.SIZE_MEDIUM,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
fun ApiKeyPreferenceRow(modifier: Modifier = Modifier) {
    val mainViewModel = LocalMainViewModel.current
    val apiKey by mainViewModel.settings.user.getApiKey().collectAsStateWithLifecycle(initialValue = ApiKey.NotAvailable)

    var placeholderKey by remember(apiKey) { mutableStateOf(apiKey) }
    PreferenceRow(
        title = stringResource(id = R.string.login_api_key),
        icon = R.drawable.key,
        modifier = modifier
    ) {
        Row {
            TextField(
                value =
                    if (placeholderKey is ApiKey.NotAvailable) ""
                    else (placeholderKey as ApiKey.Available).apiKey,
                onValueChange = { new ->
                    placeholderKey =
                        if (new.isBlank()) ApiKey.NotAvailable
                        else ApiKey.Available(apiKey = new)
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.login_api_key_not_available)
                    )
                },
                singleLine = true,
                isError = placeholderKey is ApiKey.NotAvailable || (placeholderKey as ApiKey.Available).apiKey.isBlank(),
                shape = RoundedCornerShape(
                    topStart = RoundedCornerConstants.ROUNDING_MEDIUM,
                    bottomStart = RoundedCornerConstants.ROUNDING_MEDIUM
                ),
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .height(64.dp)
                    .weight(1f)
            )

            Box(
                modifier = Modifier
                    .height(64.dp)
                    .wrapContentWidth()
                    .clip(
                        RoundedCornerShape(
                            topEnd = RoundedCornerConstants.ROUNDING_MEDIUM,
                            bottomEnd = RoundedCornerConstants.ROUNDING_MEDIUM
                        )
                    )
                    .background(TextFieldDefaults.colors().containerColor(enabled = true, isError = false, focused = true))
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Confirm this API key",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            mainViewModel.settings.user.setApiKey(
                                key = placeholderKey
                            )
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}