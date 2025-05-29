package com.example.watwallet.utils

import com.google.firebase.Timestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import java.util.Date

object DateUtils {

    fun localDateToTimestamp(date: LocalDate): Timestamp {
        val localDateTime = date.atStartOfDayIn(TimeZone.currentSystemDefault())
        return Timestamp(Date(localDateTime.toEpochMilliseconds()))
    }

    fun timestampToLocalDate(timestamp: Timestamp): LocalDate {
        return Instant.fromEpochMilliseconds(timestamp.seconds * 1000)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun millisToLocalDate(millis:Long): LocalDate {
        val instant = Instant.fromEpochMilliseconds(millis)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun localDateToMillis(date: LocalDate): Long {
        val dateTime = date.atStartOfDayIn(TimeZone.currentSystemDefault())
        return dateTime.toEpochMilliseconds()
    }

    val currentDateTime get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val currentYear = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .year

    val currentDate get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}