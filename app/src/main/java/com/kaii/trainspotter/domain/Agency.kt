package com.kaii.trainspotter.domain

import kotlinx.serialization.Serializable

@Serializable
data class Agency(
    val id: String,
    val name: String,
    val operator: String
)