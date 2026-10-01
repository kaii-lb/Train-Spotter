package com.kaii.trainspotter.helpers

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val context: Context
) {
    private val packageName = "TN_RAILWAY_DESIGNSPEED"

    private val mutex = Mutex()
    private val releaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var released = false

    private var geoPackage: GeoPackage? = null
    private var featureDao: FeatureDao? = null
    private var indexManager: FeatureIndexManager? = null
    private var cached: FeatureCollection? = null

    private fun openIfNeeded() {
        if (indexManager != null) return

        val manager = GeoPackageFactory.getManager(context)

        if (!manager.exists(packageName)) {
            context.assets.open("$packageName.gpkg").use { stream ->
                manager.importGeoPackage(packageName, stream)
            }
        }

        val gpkg = manager.open(packageName)
        val dao = gpkg.getFeatureDao(packageName)
        val index = FeatureIndexManager(context, gpkg, dao)

        if (!index.isIndexed) {
            println("FEATURES INDEXING MAY TAKE A WHILE")
            index.index()
        }

        geoPackage = gpkg
        featureDao = dao
        indexManager = index
    }

    private suspend fun queryLocked(): FeatureCollection? {
        if (released) return null
        cached?.let { return it }

        openIfNeeded()

        val dao = featureDao ?: return null
        val index = indexManager ?: return null

        val mapProjection = ProjectionFactory.getProjection(
            ProjectionConstants.AUTHORITY_EPSG,
            ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM.toLong()
        )
        val transformer = ProjectionTransform(dao.projection, mapProjection)

        val features = mutableListOf<Feature>()

        index.query(false).let { results ->
            for (featureRow in results) {
                currentCoroutineContext().ensureActive()
                if (released) return null

                val speed = featureRow.getValue("speed")?.toString()
                val geometry = featureRow.geometry?.geometry

                if (geometry != null && !geometry.isEmpty && speed != null) {
                    val input = ProjCoordinate(geometry.centroid.x, geometry.centroid.y)
                    val transformed = transformer.transform(input)

                    val feature = Feature.fromGeometry(
                        Point.fromLngLat(transformed.x, transformed.y)
                    )
                    feature.addStringProperty("speed", speed)

                    features.add(feature)
                }
            }

            results.close()
        }

        return FeatureCollection.fromFeatures(features).also { cached = it }
    }

    private suspend fun loadFeatures(): FeatureCollection? = withContext(Dispatchers.IO) {
        mutex.withLock { queryLocked() }
    }

    fun release() {
        if (released) return
        released = true

        releaseScope.launch {
            mutex.withLock {
                runCatching { indexManager?.close() }
                runCatching { geoPackage?.close() }

                indexManager = null
                featureDao = null
                geoPackage = null
                cached = null
            }

            releaseScope.cancel()
        }
    }

    suspend fun fetchSpeedsForBounds(
        mapLibreMap: MapLibreMap
    ) {
        // TODO: filter by map bounds with indexManager.query(boundingBox, projection)
        val collection = loadFeatures() ?: return

        withContext(Dispatchers.Main) {
            if (released) return@withContext

            mapLibreMap.style
                ?.getSourceAs<GeoJsonSource>("speed-source")
                ?.setGeoJson(collection)
        }
    }
}