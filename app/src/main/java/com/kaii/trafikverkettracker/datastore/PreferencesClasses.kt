package com.kaii.trafikverkettracker.datastore

interface ApiKey {
    object NotAvailable : ApiKey

    data class Available(val apiKey: String) : ApiKey
}