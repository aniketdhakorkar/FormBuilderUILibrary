package util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun convertDateToMillis(dateString: String): Long {
    val parts = dateString.split("/")
    if (parts.size != 3) throw IllegalArgumentException("Invalid date format")

    val day = parts[0].toInt()
    val month = parts[1].toInt()
    val year = parts[2].toInt()

    val localDate = LocalDate(year, month, day)
    val localDateTime = localDate.atTime(0, 0) // Midnight time
    return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun convertMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return localDate.toString()
}