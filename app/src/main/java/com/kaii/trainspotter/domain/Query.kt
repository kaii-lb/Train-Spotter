package com.kaii.trainspotter.domain

import kotlinx.serialization.Serializable

@Serializable
data class Query(
    val queryTime: String,
    val query: String
)