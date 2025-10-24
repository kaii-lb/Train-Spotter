package com.kaii.trainspotter.compose.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.kaii.trainspotter.R

enum class TimeTableType {
    Arrivals,
    Departures
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeTableTypeDisplay(
    type: TimeTableType,
    setType: (type: TimeTableType) -> Unit,
    modifier: Modifier = Modifier
) {
    val resources = LocalResources.current

    ButtonGroup(
        overflowIndicator = {},
        modifier = modifier
            .fillMaxWidth(1f)
            .padding(horizontal = 12.dp)
    ) {
        toggleableItem(
            checked = type == TimeTableType.Arrivals,
            label = resources.getString(R.string.timetable_arrivals),
            onCheckedChange = {
                if (it) setType(TimeTableType.Arrivals)
            },
            weight = 1f
        )

        toggleableItem(
            checked = type == TimeTableType.Departures,
            label = resources.getString(R.string.timetable_departures),
            onCheckedChange = {
                if (it) setType(TimeTableType.Departures)
            },
            weight = 1f
        )
    }
}