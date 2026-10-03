package com.kaii.trainspotter.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.kaii.trainspotter.R
import com.kaii.trainspotter.domain.tracking.TrackingState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrainUpdateService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "com.kaii.trainspotter.data.train_update_service"
        private const val ACTION_HIDE_NOTIF = "com.kaii.trainspotter.data.hide_status_notification"

        fun start(context: Context) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

            ContextCompat.startForegroundService(
                context,
                Intent(context, TrainUpdateService::class.java)
            )
        }
    }

    @Inject
    lateinit var tracker: TrainTracker

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    private lateinit var notificationManager: NotificationManager

    private val hideIntent by lazy {
        PendingIntent.getService(
            this,
            100,
            Intent(this, TrainUpdateService::class.java).apply {
                action = ACTION_HIDE_NOTIF
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private val openAppIntent by lazy {
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this,
                101,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.service_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.description = getString(R.string.service_channel_description)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HIDE_NOTIF) {
            // hide notification but keep tracking
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(tracker.state.value.toContent()),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )

        if (observeJob?.isActive != true) {
            observeJob = scope.launch {
                tracker.state
                    .takeWhile { it.trainId != null } // stop when the app stops tracking
                    .map { it.toContent() }
                    .distinctUntilChanged()
                    .collect {
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildNotification(it)
                        )
                    }

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private data class Content(
        val trainId: String,
        val title: String,
        val text: String,
        val progress: Int,
        val max: Int
    )

    private fun TrackingState.toContent(): Content {
        val stops = stops
        val stop = nextStop

        if (hasArrived) {
            return Content(
                trainId = trainId.orEmpty(),
                title = getString(R.string.reached_location, stop?.name),
                text = getString(R.string.stopped),
                progress = stops.size,
                max = stops.size
            )
        }

        val name = stop?.name ?: getString(R.string.loading)

        val delay = stop?.delay
            ?.takeIf { it.isNotBlank() }
            ?.let { getString(R.string.service_delay, it) }

        val speed = speedKmh?.let {
            if (position?.speedIsEstimate == true) getString(R.string.service_speed_estimate, it)
            else getString(R.string.service_speed, it)
        }

        return Content(
            trainId = trainId.orEmpty(),
            title = listOfNotNull(name, delay).joinToString(" "),
            text = speed.orEmpty(),
            progress = nextStopIndex.coerceAtLeast(0),
            max = stops.size
        )
    }

    private fun buildNotification(content: Content): Notification {
        val builder = Notification.Builder(applicationContext, CHANNEL_ID)
            .setSubText(getString(R.string.service_notification_title, content.trainId))
            .setShowWhen(false)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setContentIntent(openAppIntent)
            .setSmallIcon(R.drawable.train_filled_48px)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setActions(
                Notification.Action.Builder(
                    Icon.createWithResource(applicationContext, R.drawable.close),
                    getString(R.string.service_notification_hide),
                    hideIntent
                ).build()
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            if (content.text.isNotBlank()) builder.setShortCriticalText(content.text)

            builder.setStyle(
                Notification.ProgressStyle().apply {
                    this.progress = content.progress
                    this.isStyledByProgress = true

                    if (content.max > 2) {
                        addProgressSegment(Notification.ProgressStyle.Segment(1))
                        addProgressSegment(Notification.ProgressStyle.Segment(content.max - 2))
                        addProgressSegment(Notification.ProgressStyle.Segment(1))
                    }
                }
            )
        } else {
            builder.setProgress(content.max, content.progress, false)
        }

        return builder.build()
    }
}