package com.kaii.trainspotter.api

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class SpeedEstimator(private val windowSize: Int = 4) {
    private companion object {
        const val EARTH_RADIUS_KM = 6371.2
        const val MIN_INTERVAL_SECONDS = 5.0
        const val MAX_PLAUSIBLE_KMH = 400.0
        const val STOPPED_BELOW_KMH = 3.0
    }

    private class Sample(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Double
    )

    private var anchor: Sample? = null
    private val window = ArrayDeque<Double>()
    private var lastSpeed = 0

    fun update(
        latitude: Double,
        longitude: Double,
        timestamp: Double
    ): Int {
        val current = Sample(latitude, longitude, timestamp)
        val previous = anchor

        if (previous == null) {
            anchor = current
            return lastSpeed
        }

        val elapsed = current.timestamp - previous.timestamp

        // out of order events
        if (elapsed < 0) {
            anchor = current
            window.clear()

            return lastSpeed
        }

        // too close - will result in bad estimate
        if (elapsed < MIN_INTERVAL_SECONDS) return lastSpeed

        anchor = current

        val kmh = distanceKm(previous, current) / (elapsed / 3600.0)
        if (kmh > MAX_PLAUSIBLE_KMH) return lastSpeed

        window.addLast(kmh)
        if (window.size > windowSize) window.removeFirst()

        val average = window.average()
        lastSpeed = if (average < STOPPED_BELOW_KMH) 0 else average.roundToInt()

        return lastSpeed
    }

    private fun distanceKm(a: Sample, b: Sample): Double {
        val phi1 = a.latitude * (PI / 180.0)
        val phi2 = b.latitude * (PI / 180.0)
        val dPhi = phi2 - phi1
        val dLambda = (b.longitude - a.longitude) * (PI / 180.0)

        val sinDPhi = sin(dPhi / 2)
        val sinDLambda = sin(dLambda / 2)
        val hav = sinDPhi * sinDPhi + cos(phi1) * cos(phi2) * sinDLambda * sinDLambda

        return 2.0 * EARTH_RADIUS_KM * asin(sqrt(hav))
    }
}