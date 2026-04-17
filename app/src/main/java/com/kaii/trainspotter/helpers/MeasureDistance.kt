package com.kaii.trainspotter.helpers

import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement

fun measureDistance(
    route: List<Point>
) = TurfMeasurement.length(LineString.fromLngLats(route), TurfConstants.UNIT_KILOMETERS)
