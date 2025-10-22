package com.kaii.trafikverkettracker.compose.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kaii.trafikverkettracker.R
import com.kaii.trafikverkettracker.helpers.TextStylingConstants
import kotlinx.coroutines.delay

@Composable
fun StopSearchField(
    text: String,
    placeholder: String,
    @DrawableRes icon: Int,
    setText: (text: String) -> Unit,
    onSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(text) {
        if (text.isBlank()) return@LaunchedEffect

        delay(500)
        onSearch()
    }

    TextField(
        value = text,
        onValueChange = {
            setText(it)
        },
        maxLines = 1,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = TextStylingConstants.SIZE_MEDIUM
            )
        },
        prefix = {
            Row {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Search Icon",
                    modifier = Modifier
                        .size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
        },
        suffix = {
            if (text.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Clear search query",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            setText("")
                        }
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                keyboardController?.hide()
            }
        ),
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
    )
}