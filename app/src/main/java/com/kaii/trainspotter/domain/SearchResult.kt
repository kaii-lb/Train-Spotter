package com.kaii.trainspotter.domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val name: SearchName,
    val description: SearchDescription,
    val id: String,
    val hasError: Boolean,
    val mode: SearchMode
)