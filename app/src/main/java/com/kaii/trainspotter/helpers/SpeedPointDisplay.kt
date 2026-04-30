package com.kaii.trainspotter.helpers

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mil.nga.geopackage.GeoPackage
import mil.nga.geopackage.GeoPackageFactory
import mil.nga.geopackage.features.index.FeatureIndexManager
import mil.nga.geopackage.features.user.FeatureDao
import mil.nga.proj.ProjectionConstants
import mil.nga.proj.ProjectionFactory
import mil.nga.proj.ProjectionTransform
import org.locationtech.proj4j.ProjCoordinate
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

class SpeedPointDisplay(
    context: Context
) {
    private val packageName = "TN_RAILWAY_DESIGNSPEED"

    private val geoPackageManager = GeoPackageFactory.getManager(context)
    private val geoPackage: GeoPackage
    private val featureDao: FeatureDao
    private val indexManager: FeatureIndexManager

    private var initialized = false

    init {
        if (!geoPackageManager.exists(packageName)) {
            val assetStream = context.assets.open("$packageName.gpkg")

            geoPackageManager.importGeoPackage(packageName, assetStream)
        }

        geoPackage = geoPackageManager.open(packageName)
        featureDao = geoPackage.getFeatureDao(packageName)
        indexManager = FeatureIndexManager(context, geoPackage, featureDao)

        if (!indexManager.isIndexed) {
            println("FEATURES INDEXING MAY TAKE A WHILE}")
            indexManager.index()
        }

        initialized = true
    }

    fun release() {
        if (!initialized) return

        initialized = false

        indexManager.close()
        geoPackage.close()
    }

    suspend fun fetchSpeedsForBounds(
        mapLibreMap: MapLibreMap
    ) = withContext(Dispatchers.IO) {
        if (!initialized) return@withContext

        // TODO
        // val boundingBox = BoundingBox(sw.longitude, ne.longitude, sw.latitude, ne.latitude)

        val mapProjection = ProjectionFactory.getProjection(
            ProjectionConstants.AUTHORITY_EPSG,
            ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM.toLong()
        )
        val transformer = ProjectionTransform(featureDao.projection, mapProjection)

        val results = indexManager.query(false)

        val mapboxFeatures = mutableListOf<Feature>()
        try {
            results.forEach { featureRow ->
                val speed = featureRow.getValue("speed")?.toString()
                val geometry = featureRow.geometry.geometry

                if (geometry != null && !geometry.isEmpty && speed != null) {
                    val input = ProjCoordinate(geometry.centroid.x, geometry.centroid.y)
                    val transformedGeometry = transformer.transform(input)

                    val lat = transformedGeometry.y
                    val lon = transformedGeometry.x

                    val point = Point.fromLngLat(lon, lat)

                    val feature = Feature.fromGeometry(point)
                    feature.addStringProperty("speed", speed)

                    mapboxFeatures.add(feature)
                }
            }
        } finally {
            results.close()
            indexManager.close()
        }

        val featureCollection = FeatureCollection.fromFeatures(mapboxFeatures)
        withContext(Dispatchers.Main) {
            val source = mapLibreMap.style?.getSourceAs<GeoJsonSource>("speed-source")
            source?.setGeoJson(featureCollection)
        }
    }
}
