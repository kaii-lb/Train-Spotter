package com.kaii.trainspotter.compose.widgets.search

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.domain.SearchMode
import com.kaii.trainspotter.helpers.RoundedCornerConstants
import com.kaii.trainspotter.presentation.description
import com.kaii.trainspotter.presentation.icon

@Composable
fun SearchModeSelector(
    searchMode: () -> SearchMode,
    onSearchModeChange: (SearchMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    FilledTonalIconToggleButton(
        checked = expanded,
        onCheckedChange = { expanded = it },
        shapes = IconButtonDefaults.toggleableShapes(),
        colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
            containerColor = Color.Transparent,
            checkedContainerColor = MaterialTheme.colorScheme.primary,
            checkedContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            painter = painterResource(id = searchMode().icon),
            contentDescription = "Searching for ${searchMode().name}"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        shape = RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_LARGE),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .padding(start = 2.dp, end = 4.dp)
    ) {
        SearchMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = mode.description),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = mode.icon),
                        contentDescription = "Search for ${mode.name}",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                onClick = {
                    onSearchModeChange(mode)
                    expanded = false
                },
                shape = RoundedCornerShape(size = RoundedCornerConstants.ROUNDING_MEDIUM)
            )
        }
    }
}