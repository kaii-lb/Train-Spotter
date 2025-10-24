package com.kaii.trainspotter.helpers

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun formatSecondsToTime(
    seconds: Long
): String =
    Instant.fromEpochSeconds(seconds)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .format(
            format = LocalDateTime.Format {
                hour()
                char(':')
                minute()
            }
        )

fun formatDelay(
    delay: Int
): String {
    val prefix = if (delay < 0) "-" else ""

    val formatted = Duration.parse("${abs(delay)}s")

    return prefix + formatted
}
