package com.kaii.trainspotter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.kaii.trainspotter.api.LocationDetails
import com.kaii.trainspotter.api.TrafikverketClient
import com.kaii.trainspotter.api.TrainPositionClient
import com.kaii.trainspotter.helpers.ServerConstants
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "com.kaii.trainspotter.TrainUpdateService"

class TrainUpdateService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "train_update_service"
    }

    private lateinit var notificationManager: NotificationManager
    private var running = false
    private var job: Job? = null

    private lateinit var trafikverketClient: TrafikverketClient
    private lateinit var trainPositionClient: TrainPositionClient

    private var trainId: String? = null
    private var announcements = sortedMapOf<String, LocationDetails>()
    private val binder = TrainUpdateBinder()

    private var currentProgress = 0
    private var currentTitle = ""
    private var currentSpeed = ""

    inner class TrainUpdateBinder : Binder() {
        val service = this@TrainUpdateService
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, "Train Spotter Channel", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Handles notification updates"

        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun setup(
        apiKey: String,
        trainId: String,
        initialTitle: String,
        initialSpeed: String
    ) {
        this.trainId = trainId
        this.currentTitle = initialTitle
        this.currentSpeed = initialSpeed

        this.trafikverketClient =
            TrafikverketClient(
                context = applicationContext,
                apiKey = apiKey
            )

        this.trainPositionClient =
            TrainPositionClient(
                context = applicationContext,
                apiKey = apiKey
            )
    }

    fun stopListening() {
        stopForeground(STOP_FOREGROUND_REMOVE)

        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.cancelAll()
        notificationManager.deleteNotificationChannel(CHANNEL_ID)

        running = false
        job?.cancel()
        trainId = null
        trainPositionClient.cancel()

        Log.d(TAG, "Service canceled.")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startListening() {
        this.running = true

        val notification = buildNotification(
            progress = 0,
            contentTitle = currentTitle,
            speed = currentSpeed
        )

        startForeground(NOTIFICATION_ID, notification)
        notificationManager.notify(NOTIFICATION_ID, notification)

        job?.cancel()
        job = GlobalScope.launch(Dispatchers.IO) {
            coroutineScope {
                fetchStopData()
                fetchPositionData()

                while (running) {
                    fetchStopData()

                    delay(ServerConstants.UPDATE_TIME)
                }
            }
        }
    }

    private suspend fun fetchStopData() {
        val new = trafikverketClient.getRouteDataForId(trainId = trainId!!)
        announcements = new

        val position = announcements.values.firstOrNull { value ->
            !value.passed
        } ?: announcements.values.last()

        if (position == announcements.values.last()) {
            this.currentSpeed = applicationContext.resources.getString(R.string.stopped)
            this.currentProgress = announcements.keys.size
            this.currentTitle = applicationContext.resources.getString(R.string.reached_location, position.name)
        } else {
            val delay =
                if (position?.delay != null && position.delay.isNotBlank()) {
                    "with ${position.delay} delay"
                } else ""

            this.currentTitle = "${position?.name ?: "Unknown"} $delay"
        }

        val updatedNotification = buildNotification(
            progress = this.currentProgress,
            contentTitle = this.currentTitle,
            speed = this.currentSpeed
        )

        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    private suspend fun fetchPositionData() = withContext(Dispatchers.IO) {
        if (trainPositionClient.getCurrentTrainId() == trainId || announcements.isEmpty()) return@withContext

        trainPositionClient.getStreamingInfo(
            trainId = trainId!!
        ) { info ->
            val lastKey = announcements.keys.lastOrNull()
            val speed = if (announcements[lastKey]?.passed == true) 0 else info.speed
            val speedIsEstimate = info.speedIsEstimate

            val key = announcements.keys.firstOrNull { key ->
                announcements[key]?.passed == false
            } ?: announcements.keys.last()

            val position = announcements[key]

            if (position == announcements.values.last()) {
                this@TrainUpdateService.currentSpeed = applicationContext.resources.getString(R.string.stopped)
                this@TrainUpdateService.currentTitle = applicationContext.resources.getString(R.string.reached_location, position.name)
                this@TrainUpdateService.currentProgress = announcements.keys.size
            } else {
                val delay =
                    if (position?.delay != null && position.delay.isNotBlank()) {
                        "with ${position.delay} delay"
                    } else ""

                this@TrainUpdateService.currentTitle = "${position?.name ?: "Unknown"} $delay"
                this@TrainUpdateService.currentSpeed = "${speed}km/h" + if (speedIsEstimate) "*" else ""
                this@TrainUpdateService.currentProgress = announcements.keys.indexOf(key)
            }


            val updatedNotification = buildNotification(
                progress = this@TrainUpdateService.currentProgress,
                contentTitle = this@TrainUpdateService.currentTitle,
                speed = this@TrainUpdateService.currentSpeed
            )

            notificationManager.notify(NOTIFICATION_ID, updatedNotification)
        }
    }

    private fun buildNotification(
        progress: Int,
        contentTitle: String,
        speed: String
    ): Notification {
        val notification =
            Notification.Builder(this, CHANNEL_ID)
                .setSubText("Train: ${trainId!!}")
                .setShowWhen(false)
                .setContentTitle(contentTitle)
                .setContentText(speed)
                .setSmallIcon(R.drawable.train_filled_48px)
                .setOngoing(true)
                .setOnlyAlertOnce(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            notification.style =
                Notification.ProgressStyle().apply {
                    this.progress = progress
                    this.isStyledByProgress = true

                    if (announcements.keys.size > 2) {
                        addProgressSegment(
                            Notification.ProgressStyle.Segment(1)
                        )
                        addProgressSegment(
                            Notification.ProgressStyle.Segment(announcements.keys.size - 2)
                        )
                        addProgressSegment(
                            Notification.ProgressStyle.Segment(1)
                        )
                    }
                }
        } else {
            notification.setProgress(announcements.keys.size, progress, false)
        }

        return notification.build()
    }
}

class TrainUpdateConnection : ServiceConnection {
    var service: TrainUpdateService? = null

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as TrainUpdateService.TrainUpdateBinder
        this.service = binder.service
        Log.d(TAG, "Service was connected, ${this.service!!::class.simpleName}")
    }

    override fun onServiceDisconnected(className: ComponentName) {
        this.service = null
        Log.d(TAG, "Service was disconnected")
    }
}