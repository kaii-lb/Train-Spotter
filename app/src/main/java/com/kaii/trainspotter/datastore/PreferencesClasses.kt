package com.kaii.trainspotter.datastore

import androidx.compose.runtime.saveable.listSaver

interface ApiKey {
    object NotAvailable : ApiKey

    data class Available(
        val realtimeKey: String,
        val trafikverketKey: String
    ) : ApiKey

    companion object {
        val Saver = listSaver(
            save = {
                if (it is Available) {
                    listOf(
                        it.realtimeKey,
                        it.trafikverketKey
                    )
                } else {
                    emptyList()
                }
            },
            restore = {
                if (it.isEmpty()) {
                    NotAvailable
                } else {
                    Available(
                        realtimeKey = it[0],
                        trafikverketKey = it[1]
                    )
                }
            }
        )
    }
}