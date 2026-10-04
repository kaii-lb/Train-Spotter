package com.kaii.trainspotter.helpers

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import com.kaii.trainspotter.R
import com.kaii.trainspotter.domain.search.SearchDescription
import com.kaii.trainspotter.domain.search.SearchName
import com.kaii.trainspotter.domain.station.TransportMode
import com.kaii.trainspotter.presentation.label

class ResultInfoFetcher(
    private val resources: Resources
) {
    fun getFromName(name: SearchName) = when (name) {
        is SearchName.Station -> name.name
        is SearchName.TrainRoute -> getTrainRoute(name.start, name.end)
    }

    fun getFromDescription(description: SearchDescription) = when (description) {
        is SearchDescription.Station -> getStationDescription(description.modes)
        is SearchDescription.TrainTime -> getTrainTime(description.departure, description.arrival)
    }

    private fun getStationDescription(modes: List<TransportMode>): String = resources.getString(
        R.string.search_transport_modes,
        modes.joinToString {
            resources.getString(it.label)
        }
    )

    private fun getTrainRoute(
        start: String,
        end: String
    ): String = resources.getString(
        R.string.search_train_route,
        start, end
    )

    private fun getTrainTime(
        departure: String,
        arrival: String
    ): String = resources.getString(
        R.string.search_train_time,
        departure,
        arrival
    )
}

@Composable
fun rememberResultInfoFetcher(): ResultInfoFetcher {
    val resources = LocalResources.current

    return remember {
        ResultInfoFetcher(resources)
    }
}