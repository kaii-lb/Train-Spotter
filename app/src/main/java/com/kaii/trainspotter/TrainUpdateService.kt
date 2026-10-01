package com.kaii.trainspotter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.datastore.ApiKey
import com.kaii.trainspotter.domain.LocationDetails
import com.kaii.trainspotter.helpers.ServerConstants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "com.kaii.trainspotter.TrainUpdateService"

class TrainUpdateService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "com.kaii.trainspotter.train_update_service"

        const val ACTION_HIDE_NOTIF = "com.kaii.trainspotter.hide_status_notification"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var notificationManager: NotificationManager
    private var job: Job? = null

    @Volatile
    private var running = false

    @Volatile
    private var trainId: String? = null

    private var trafikverketClient: TrafikverketClient? = null
    private var trainPositionClient: TrainPositionClient? = null

    @Volatile
    private var announcements: Map<String, LocationDetails> = emptyMap()

    private val binder = TrainUpdateBinder()

    @Volatile
    private var currentProgress = 0

    @Volatile
    private var currentTitle = ""

    @Volatile
    private var currentSpeed = ""

    @Volatile
    private var speedIsEstimate = false

    inner class TrainUpdateBinder : Binder() {
        val service = this@TrainUpdateService
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_HIDE_NOTIF -> stopListening()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false
        trainPositionClient?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    fun setup(
        apiKey: ApiKey,
        trainId: String,
        initialTitle: String,
        initialSpeed: String
    ) {
        running = false
        job?.cancel()
        trainPositionClient?.cancel()

        this.trainId = trainId
        this.currentTitle = initialTitle
        this.currentSpeed = initialSpeed
        this.currentProgress = 0
        this.speedIsEstimate = false
        this.announcements = emptyMap()

        this.trafikverketClient = TrafikverketClient(apiKey = apiKey)
        this.trainPositionClient = TrainPositionClient(apiKey = apiKey)
    }

    fun stopListening() {
        running = false
        trainId = null
        trainPositionClient?.cancel()
        job?.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        notificationManager.cancel(NOTIFICATION_ID)

        Log.d(TAG, "Service canceled.")
    }

    fun startListening() {
        val id = trainId ?: return
        val client = trafikverketClient ?: return

        running = true

        val channel = NotificationChannel(CHANNEL_ID, "Train Spotter Channel", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Handles notification updates"
        notificationManager.createNotificationChannel(channel)

        val notification = buildNotification(
            trainId = id,
            progress = 0,
            contentTitle = currentTitle,
            speed = currentSpeed,
            speedIsEstimate = speedIsEstimate
        )

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )

        job?.cancel()
        job = scope.launch {
            try {
                fetchStopData(id, client)
                launch { fetchPositionData(id) }

                while (isActive && running) {
                    delay(ServerConstants.UPDATE_TIME.milliseconds)
                    fetchStopData(id, client)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Update loop failed", e)
            }
        }
    }

    private suspend fun fetchStopData(id: String, client: TrafikverketClient) {
        if (!running) return

        try {
            val new = client.getRouteDataForId(trainId = id)
            if (!running || trainId != id) return

            if (new.isNullOrEmpty()) {
                Log.w(TAG, "No route data for $id (null or empty), keeping previous")
                return
            }

            announcements = new
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch stop data", e)
            return
        }

        val values = announcements.values
        val position = values.firstOrNull { !it.passed } ?: values.lastOrNull()

        if (position == values.lastOrNull() && position?.passed == true && !currentSpeed.startsWith("0km/h")) {
            currentSpeed = applicationContext.resources.getString(R.string.stopped)
            currentProgress = announcements.keys.size
            currentTitle = applicationContext.resources.getString(R.string.reached_location, position.name)
        } else {
            currentTitle = "${position?.name ?: "Unknown"} ${delayText(position)}"
        }

        postNotification(id)
    }

    private suspend fun fetchPositionData(id: String) {
        val client = trainPositionClient ?: return
        if (client.getCurrentTrainId() == id || announcements.isEmpty() || !running) return

        try {
            client.getStreamingInfo(trainId = id) { info ->
                if (!running || trainId != id) return@getStreamingInfo

                val current = announcements
                if (current.isEmpty()) return@getStreamingInfo

                val speed = if (current.values.lastOrNull()?.passed == true) 0 else info.speed
                val estimate = info.speedIsEstimate

                val key = current.keys.firstOrNull { current[it]?.passed == false } ?: current.keys.last()
                val position = current[key]

                if (position == current.values.lastOrNull() && position?.passed == true && !currentSpeed.startsWith("0km/h")) {
                    currentSpeed = applicationContext.resources.getString(R.string.stopped)
                    currentTitle = applicationContext.resources.getString(R.string.reached_location, position.name)
                    currentProgress = current.keys.size
                } else {
                    currentTitle = "${position?.name ?: "Unknown"} ${delayText(position)}"
                    currentSpeed = "${speed}km/h"
                    currentProgress = current.keys.indexOf(key)
                    speedIsEstimate = estimate
                }

                postNotification(id)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Position stream failed", e)
        }
    }

    private fun delayText(position: LocationDetails?): String =
        if (position?.delay != null && position.delay.isNotBlank()) "with ${position.delay} delay" else ""

    private fun postNotification(id: String) {
        if (!running || trainId != id) return

        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification(
                trainId = id,
                progress = currentProgress,
                contentTitle = currentTitle,
                speed = currentSpeed,
                speedIsEstimate = speedIsEstimate
            )
        )
    }

    private fun buildNotification(
        trainId: String,
        progress: Int,
        contentTitle: String,
        speed: String,
        speedIsEstimate: Boolean
    ): Notification {
        val hideIntent = PendingIntent.getForegroundService(
            applicationContext,
            100,
            Intent(applicationContext, TrainUpdateService::class.java).apply {
                action = ACTION_HIDE_NOTIF
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                applicationContext,
                101,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val speedText = if (speedIsEstimate) "${speed}*" else speed

        val notification =
            Notification.Builder(applicationContext, CHANNEL_ID)
                .setSubText("Train: $trainId")
                .setShowWhen(false)
                .setContentTitle(contentTitle)
                .setContentText(speedText)
                .setContentIntent(openAppIntent)
                .setSmallIcon(R.drawable.train_filled_48px)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        setShortCriticalText(speedText)
                    }
                }
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setActions(
                    Notification.Action.Builder(
                        Icon.createWithResource(applicationContext, R.drawable.close),
                        "Hide",
                        hideIntent
                    ).build()
                )

        val stopCount = announcements.keys.size

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            notification.style =
                Notification.ProgressStyle().apply {
                    this.progress = progress
                    this.isStyledByProgress = true

                    if (stopCount > 2) {
                        addProgressSegment(Notification.ProgressStyle.Segment(1))
                        addProgressSegment(Notification.ProgressStyle.Segment(stopCount - 2))
                        addProgressSegment(Notification.ProgressStyle.Segment(1))
                    }
                }
        } else {
            notification.setProgress(stopCount, progress, false)
        }

        return notification.build()
    }
}