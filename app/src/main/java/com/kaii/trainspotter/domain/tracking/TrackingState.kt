package com.kaii.trainspotter.domain.tracking

import com.kaii.trainspotter.domain.LocationDetails
import com.kaii.trainspotter.domain.TrainPositionMini

sealed interface RouteState {
    data object Idle : RouteState
    data object Loading : RouteState
    data object Failed : RouteState
    data class Loaded(val stops: List<LocationDetails>) : RouteState
}


data class TrackingState(
    val trainId: String? = null,
    val routeState: RouteState = RouteState.Idle,
    val position: TrainPositionMini? = null,
) {
    val stops: List<LocationDetails>
        get() = (routeState as? RouteState.Loaded)?.stops.orEmpty()

    val hasArrived: Boolean
        get() = stops.lastOrNull()?.passed == true

    val nextStopIndex: Int
        get() = stops.indexOfFirst { !it.passed }.takeIf { it >= 0 } ?: stops.lastIndex

    val nextStop: LocationDetails?
        get() = stops.getOrNull(nextStopIndex)

    val speedKmh: Int?
        get() = if (hasArrived) 0 else position?.speed?.takeIf { it >= 0 }
}