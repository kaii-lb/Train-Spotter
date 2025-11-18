package com.kaii.trainspotter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrainPositionResponseHolder(
    @SerialName("RESPONSE")
    val response: TrainPositionResponse
)

@Serializable
data class TrainPositionResponse(
    @SerialName("RESULT")
    val result: List<TrainPositionResult>
)

@Serializable
data class TrainPositionResult(
    @SerialName("TrainPosition")
    val trainPosition: List<TrainPosition> = emptyList(),

    @SerialName("ERROR")
    val error: ErrorDetails? = null,

    @SerialName("INFO")
    val info: InfoDetails? = null,

    @SerialName("id")
    val id: String? = null
)

@Serializable
data class TrainPosition(
    @SerialName("Train")
    val train: TrainPositionTrainInfo? = null,

    @SerialName("Position")
    val position: TrainPositionInfo? = null,

    @SerialName("TimeStamp")
    val timeStamp: String? = null,

    @SerialName("Status")
    val status: Status? = null,

    @SerialName("Bearing")
    val bearing: Int? = null,

    @SerialName("Speed")
    val speed: Int? = null,

    @SerialName("VersionNumber")
    val versionNumber: Long? = null,

    @SerialName("ModifiedTime")
    val modifiedTime: String? = null,

    @SerialName("Deleted")
    val deleted: Boolean? = null
)

@Serializable
data class TrainPositionTrainInfo(
    @SerialName("OperationalTrainNumber")
    val operationalTrainNumber: String? = null,

    @SerialName("OperationalTrainDepartureDate")
    val operationalTrainDepartureDate: String? = null,

    @SerialName("JourneyPlanNumber")
    val journeyPlanNumber: String? = null,

    @SerialName("JourneyPlanDepartureDate")
    val journeyPlanDepartureDate: String? = null,

    @SerialName("AdvertisedTrainNumber")
    val advertisedTrainNumber: String? = null
)

@Serializable
data class TrainPositionInfo(
    @SerialName("SWEREF99TM")
    val swereF99tm: String? = null,

    // Coordinates in WGS84 format (WKT string)
    @SerialName("WGS84")
    val wgs84: String? = null
)

@Serializable
data class Status(
    @SerialName("Active")
    val active: Boolean? = null
)

@Serializable
data class ErrorDetails(
    @SerialName("SOURCE")
    val source: String? = null,

    @SerialName("MESSAGE")
    val message: String? = null
)

@Serializable
data class InfoDetails(
    @SerialName("LASTMODIFIED")
    val lastModified: String? = null,

    @SerialName("LASTCHANGEID")
    val lastChangeId: String? = null,

    @SerialName("EVALRESULT")
    val evalResult: List<String>? = null,

    @SerialName("SSEURL")
    val sseUrl: String? = null
)