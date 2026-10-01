package com.kaii.trainspotter.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kaii.trainspotter.R
import com.kaii.trainspotter.domain.SearchMode

@get:DrawableRes
val SearchMode.icon: Int
    get() = when (this) {
        SearchMode.Station -> R.drawable.location_on_filled
        SearchMode.Train -> R.drawable.train_filled
    }

@get:StringRes
val SearchMode.description: Int
    get() = when (this) {
        SearchMode.Station -> R.string.station
        SearchMode.Train -> R.string.train
    }