package com.kaii.trainspotter

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class TrainUpdateConnection : ServiceConnection {
    var service: TrainUpdateService? = null

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as TrainUpdateService.TrainUpdateBinder
        this.service = binder.service
        Log.d(TrainUpdateConnection::class.qualifiedName, "Service was connected, ${this.service!!::class.simpleName}")
    }

    override fun onServiceDisconnected(className: ComponentName) {
        this.service = null
        Log.d(TrainUpdateConnection::class.qualifiedName, "Service was disconnected")
    }
}