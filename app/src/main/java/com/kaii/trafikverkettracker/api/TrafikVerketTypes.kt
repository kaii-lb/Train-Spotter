package com.kaii.trafikverkettracker.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

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
data class TrainAnnouncement(
    @SerialName("ActivityId")
    val activityId: String? = null,

    @SerialName("ActivityType")
    val activityType: String? = null,

    @SerialName("Advertised")
    val advertised: Boolean? = null,

    @SerialName("AdvertisedTimeAtLocation")
    val advertisedTimeAtLocation: String? = null,

    @SerialName("AdvertisedTrainIdent")
    val advertisedTrainId: String? = null,

    @SerialName("Booking")
    val booking: List<String>? = null,

    @SerialName("Canceled")
    val canceled: Boolean? = null,

    @SerialName("Deleted")
    val deleted: Boolean? = null,

    @SerialName("DepartureDateOTN")
    val departureDateOTN: String? = null,

    @SerialName("Deviation")
    val deviation: List<String>? = null,

    @SerialName("EstimatedTimeAtLocation")
    val estimatedTimeAtLocation: String? = null,

    @SerialName("EstimatedTimeIsPreliminary")
    val estimatedTimeIsPreliminary: Boolean? = null,

    @SerialName("FromLocation")
    val fromLocation: List<Location>? = null,

    @SerialName("InformationOwner")
    val informationOwner: String? = null,

    @SerialName("LocationDateTimeOTN")
    val locationDateTimeOTN: String? = null,

    @SerialName("LocationSignature")
    val locationSignature: String? = null,

    @SerialName("MobileWebLink")
    val mobileWebLink: String? = null,

    @SerialName("ModifiedTime")
    val modifiedTime: String? = null,

    @SerialName("NewEquipment")
    val newEquipment: Int? = null,

    @SerialName("Operator")
    val operator: String? = null,

    @SerialName("OperationalTrainNumber")
    val operationalTrainNumber: String? = null,

    @SerialName("OperationalTransportIdentifiers")
    val operationalTransportIdentifiers: List<CompositIdentifierOperationalType> = emptyList(),

    @SerialName("OtherInformation")
    val otherInformation: List<String> = emptyList(),

    @SerialName("PlannedEstimatedTimeAtLocation")
    val plannedEstimatedTimeAtLocation: String? = null,

    @SerialName("PlannedEstimatedTimeAtLocationIsValid")
    val plannedEstimatedTimeAtLocationIsValid: Boolean = false,

    @SerialName("ProductInformation")
    val productInformation: List<String> = emptyList(),

    @SerialName("ScheduledDepartureDateTime")
    val scheduledDepartureDateTime: String? = null,

    @SerialName("Service")
    val service: List<String> = emptyList(),

    @SerialName("TimeAtLocation")
    val timeAtLocation: String? = null,

    @SerialName("TimeAtLocationWithSeconds")
    val timeAtLocationWithSeconds: String? = null,

    @SerialName("ToLocation")
    val toLocation: List<Location> = emptyList(),

    @SerialName("TrackAtLocation")
    val trackAtLocation: String? = null,

    @SerialName("TrainComposition")
    val trainComposition: List<String> = emptyList(),

    @SerialName("TrainOwner")
    val trainOwner: String? = null,

    @SerialName("TypeOfTraffic")
    val typeOfTraffic: String? = null,

    @SerialName("ViaFromLocation")
    val viaFromLocation: List<Location> = emptyList(),

    @SerialName("ViaToLocation")
    val viaToLocation: List<Location> = emptyList(),
    @SerialName("WebLink")
    val webLink: String? = null,
    @SerialName("WebLinkName")
    val webLinkName: String? = null
)

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

@Serializable
data class TrainAnnouncementResult(
    @SerialName("id")
    val id: String? = null,

    @SerialName("TrainAnnouncement")
    val trainAnnouncements: List<TrainAnnouncement> = emptyList(),

    @SerialName("ERROR")
    val error: Error? = null,

    @SerialName("INFO")
    val info: Info? = null
)

@Serializable
data class Response(
    @SerialName("RESULT")
    val result: List<TrainAnnouncementResult>
)

@Serializable
data class TrainAnnouncementResponse(
    @SerialName("RESPONSE")
    val response: Response
)

@Serializable
data class LocationDetails(
    val name: String,
    val track: String,
    val arrivalTime: String,
    val departureTime: String,
    val delay: String
) {
    @OptIn(ExperimentalTime::class)
    val arrivalTimeFormatted: String
        get() {
            if (arrivalTime.isBlank()) return ""

            return LocalDateTime.parse(
                arrivalTime,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            ).atZone(ZoneId.systemDefault())
                .format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
        }

    @OptIn(ExperimentalTime::class)
    val departureTimeFormatted: String
        get() {
            if (departureTime.isBlank()) return ""

            return LocalDateTime.parse(
                departureTime,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            ).atZone(ZoneId.systemDefault())
                .format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
        }
}