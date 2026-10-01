package com.kaii.trainspotter.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    @SerialName("LocationName")
    val locationName: String? = null,

    @SerialName("Priority")
    val priority: Int? = null,

    @SerialName("Order")
    val order: Int? = null
)

@Serializable
data class Information(
    @SerialName("Code")
    val code: String,

    @SerialName("Description")
    val description: String
)

@Serializable
data class CompositIdentifierOperationalType(
    @SerialName("ObjectType")
    val objectType: String? = null,

    @SerialName("Company")
    val company: String? = null,

    @SerialName("Core")
    val core: String? = null,

    @SerialName("Variant")
    val variant: String? = null,

    @SerialName("TimetableYear")
    val timetableYear: Int? = null,

    @SerialName("StartDate")
    val startDate: String? = null
)


// --- Main Data Classes ---

@Serializable
data class Error(
    @SerialName("SOURCE")
    val source: String? = null,

    @SerialName("MESSAGE")
    val message: String? = null
)

@Serializable
data class LastModified(
    @SerialName("datetime")
    val datetime: String? = null
)

@Serializable
data class EvalResult(
    val content: String? = null
)

@Serializable
data class Info(
    @SerialName("LASTMODIFIED")
    val lastModified: LastModified? = null,

    @SerialName("LASTCHANGEID")
    val lastChangeId: String? = null,

    @SerialName("EVALRESULT")
    val evalResult: List<EvalResult> = emptyList(),

    @SerialName("SSEURL")
    val sseUrl: String? = null
)

