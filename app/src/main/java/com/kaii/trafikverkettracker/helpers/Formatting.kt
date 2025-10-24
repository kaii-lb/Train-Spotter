package com.kaii.trafikverkettracker.helpers

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

fun formatSecondsToTime(
    seconds: Long
): String =
    LocalDateTime
        .ofInstant(
            java.time.Instant.ofEpochSecond(abs(seconds)),
            ZoneId.systemDefault()
        )
        .format(
            DateTimeFormatter.ofPattern("HH:mm")
        )

fun formatDelay(
    delay: Int
): String {
    val prefix = if (delay < 0) "-" else ""

    val formatted = abs(delay).seconds.toComponents { minutes, seconds, _ ->
        String.format(
            Locale.ENGLISH,
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    return prefix + formatted
}

fun offsetIsoToSeconds(
    iso: String
) = LocalDateTime.parse(
    iso,
    DateTimeFormatter.ISO_OFFSET_DATE_TIME
).toEpochSecond(
    ZonedDateTime.now().offset
)

