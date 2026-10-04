package com.kaii.trainspotter.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kaii.trainspotter.R
import com.kaii.trainspotter.domain.station.TransportMode

@get:DrawableRes
val TransportMode.icon: Int
    get() = when (this) {
        TransportMode.Bus -> R.drawable.directions_bus
        TransportMode.Train -> R.drawable.train
        TransportMode.Taxi -> R.drawable.local_taxi
        TransportMode.Metro -> R.drawable.subway
        TransportMode.Tram -> R.drawable.tram
        TransportMode.Boat -> R.drawable.directions_boat
    }

@get:StringRes
val TransportMode.label: Int
    get() = when (this) {
        TransportMode.Bus -> R.string.transport_bus
        TransportMode.Train -> R.string.transport_train
        TransportMode.Taxi -> R.string.transport_taxi
        TransportMode.Metro -> R.string.transport_metro
        TransportMode.Tram -> R.string.transport_tram
        TransportMode.Boat -> R.string.transport_boat
    }