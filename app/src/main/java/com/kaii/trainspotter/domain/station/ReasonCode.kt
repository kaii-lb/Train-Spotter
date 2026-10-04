package com.kaii.trainspotter.domain.station

import com.kaii.trainspotter.api.Alert
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReasonCodeTrafikverketResponse(
    @SerialName("RESPONSE")
    val response: ReasonCodeResponse
)

@Serializable
data class ReasonCodeResponse(
    @SerialName("RESULT")
    val result: List<ReasonCodeResult>
)

@Serializable
data class ReasonCodeResult(
    @SerialName("ReasonCode")
    val reasonCode: List<ReasonCode>? = null,

    @SerialName("ERROR")
    val error: Error? = null,

    @SerialName("INFO")
    val info: Info? = null
)

@Serializable
data class ReasonCode(
    @SerialName("Code")
    val code: String? = null,

    @SerialName("GroupDescription")
    val groupDescription: String? = null,

    @SerialName("Level1Description")
    val level1Description: String? = null,

    @SerialName("Level2Description")
    val level2Description: String? = null,

    @SerialName("Level3Description")
    val level3Description: String? = null,

    @SerialName("ModifiedTime")
    val modifiedTime: String? = null,

    @SerialName("Deleted")
    val deleted: Boolean = false
)

fun ReasonCode.toAlert(
    additionTypeInfo: String? = null,
    isDeviation: Boolean = false
): Alert {
    val type = listOfNotNull(
        additionTypeInfo,
        level1Description,
        code
    ).joinToString(" - ")

    val text = listOfNotNull(
        groupDescription,
        level3Description
    ).joinToString(" - ")

    return Alert(
        type = type,
        title = level2Description ?: type,
        text = text,
        isDeviation = isDeviation
    )
}