package com.kaii.trainspotter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RailwayEventResponseHolder(
    @SerialName("RESPONSE")
    val response: RailwayEventResponse
)

@Serializable
data class RailwayEventResponse(
    @SerialName("RESULT")
    val railwayResult: List<RailwayResult> = emptyList()
)

@Serializable
data class RailwayResult(
    @SerialName("RailwayEvent")
    val railwayEvents: List<RailwayEvent> = emptyList(),

    @SerialName("ERROR")
    val error: RailwayError? = null,

    @SerialName("INFO")
    val info: RailwayInfo? = null,

    @SerialName("ID")
    val id: String? = null
)

@Serializable
data class RailwayError(
    @SerialName("Source")
    val source: String? = null,

    @SerialName("Message")
    val message: String? = null
)

@Serializable
data class RailwayInfo(
    @SerialName("LastModified")
    val lastModified: String? = null,

    @SerialName("LastChangedId")
    val lastChangedId: String? = null,

    @SerialName("EvalResult")
    val evalResult: List<String>? = null,

    @SerialName("SseUrl")
    val sseUrl: String? = null
)

@Serializable
data class RailwayEvent(
    @SerialName("EventId")
    val eventId: String? = null,

    @SerialName("StartDateTime")
    val startDateTime: String? = null,

    @SerialName("EndDateTime")
    val endDateTime: String? = null,

    @SerialName("ReasonCode")
    val reasonCode: String? = null,

    @SerialName("OperativeEventId")
    val operativeEventId: String? = null,

    @SerialName("EventStatus")
    val eventStatus: String? = null,

    @SerialName("Deleted")
    val deleted: Boolean? = null,

    @SerialName("Version")
    val version: Int? = null,

    @SerialName("CreatedDateTime")
    val createdDateTime: String? = null,

    @SerialName("ModifiedDateTime")
    val modifiedDateTime: String? = null,

    @SerialName("SelectedSection")
    val selectedSection: List<RailwaySelectedSection> = emptyList(),

    @SerialName("ModifiedTime")
    val modifiedTime: String? = null,
)

@Serializable
data class RailwaySelectedSection(
    @SerialName("FromLocation")
    val fromLocation: RailwayLocation? = null,

    @SerialName("ToLocation")
    val toLocation: RailwayLocation? = null,

    @SerialName("ViaLocation")
    val viaLocation: RailwayLocation? = null,

    @SerialName("IntermediateLocation")
    val intermediateLocation: List<RailwayLocation> = emptyList()
)

@Serializable
data class RailwayLocation(
    @SerialName("CountryCode")
    val countryCode: String? = null,

    @SerialName("LocationIncluded")
    val locationIncluded: Boolean? = null,

    @SerialName("LocationPrimaryCode")
    val locationPrimaryCode: String? = null,

    @SerialName("Signature")
    val signature: String? = null,

    @SerialName("LocationOrder")
    val locationOrder: Int? = null
)

@Serializable
data class RailwayEventError(
    val code: String,
    val category: String? = null,
    val subcategory: String? = null,
    val level3: String? = null,
    val usage: String? = null,
    val description: String
)