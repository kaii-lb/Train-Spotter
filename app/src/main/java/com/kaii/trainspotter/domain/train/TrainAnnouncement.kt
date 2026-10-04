package com.kaii.trainspotter.domain.train

import com.kaii.trainspotter.domain.station.CompositIdentifierOperationalType
import com.kaii.trainspotter.domain.station.Information
import com.kaii.trainspotter.domain.station.Location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val booking: List<Information> = emptyList(),

    @SerialName("Canceled")
    val canceled: Boolean? = null,

    @SerialName("Deleted")
    val deleted: Boolean? = null,

    @SerialName("DepartureDateOTN")
    val departureDateOTN: String? = null,

    @SerialName("Deviation")
    val deviations: List<Information> = emptyList(),

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
    val otherInformation: List<Information> = emptyList(),

    @SerialName("PlannedEstimatedTimeAtLocation")
    val plannedEstimatedTimeAtLocation: String? = null,

    @SerialName("PlannedEstimatedTimeAtLocationIsValid")
    val plannedEstimatedTimeAtLocationIsValid: Boolean = false,

    @SerialName("ProductInformation")
    val productInformation: List<Information> = emptyList(),

    @SerialName("ScheduledDepartureDateTime")
    val scheduledDepartureDateTime: String? = null,

    @SerialName("Service")
    val service: List<Information> = emptyList(),

    @SerialName("TimeAtLocation")
    val timeAtLocation: String? = null,

    @SerialName("TimeAtLocationWithSeconds")
    val timeAtLocationWithSeconds: String? = null,

    @SerialName("ToLocation")
    val toLocation: List<Location> = emptyList(),

    @SerialName("TrackAtLocation")
    val trackAtLocation: String? = null,

    @SerialName("TrainComposition")
    val trainComposition: List<Information> = emptyList(),

    @SerialName("TrainOwner")
    val trainOwner: String? = null,

    @SerialName("TypeOfTraffic")
    val typeOfTraffic: List<Information> = emptyList(),

    @SerialName("ViaFromLocation")
    val viaFromLocation: List<Location> = emptyList(),

    @SerialName("ViaToLocation")
    val viaToLocation: List<Location> = emptyList(),
    @SerialName("WebLink")
    val webLink: String? = null,
    @SerialName("WebLinkName")
    val webLinkName: String? = null
)