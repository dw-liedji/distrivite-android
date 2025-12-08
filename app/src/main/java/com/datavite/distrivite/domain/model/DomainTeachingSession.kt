package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class DomainTeachingSession(
    val id: String,
    val created: String,
    val modified: String,
    val orgUserId: String,
    val userId: String,
    val orgSlug: String,
    val course: String,
    val teachingPeriodId: String,
    val hourlyRemuneration: String,
    val syncStatus: SyncStatus,
    val parentsNotified: Boolean,
    val courseId: String,
    val scenario: String,
    val orgId: String,
    val room: String,
    val roomId: String,
    val start: String,
    val end: String,
    val rStart: String?, // nullable String for null value
    val rEnd: String?,   // nullable String for null value
    val day: String,
    val option: String,
    val level: String,
    val cursus: String,
    val instructorId: String,
    val instructorContractId: String,
    val educationClassId: String,
    val instructor: String,
    val klass: String,
    val status: String
) {
    fun isStarted() = !rStart.isNullOrBlank() && rStart.lowercase() != "none"
    fun isEnded() = isStarted() && !rEnd.isNullOrBlank() && rEnd.lowercase() != "none"
    fun isApproved() = isEnded() && status.contains("Accepted", ignoreCase = true)

    /** Format day field as "Tuesday, 02 July 2025" */
    fun displayDay(): String {
        return try {
            val parsed = LocalDate.parse(day)
            val weekday = parsed.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val formattedDate = parsed.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))
            "$weekday, $formattedDate"
        } catch (e: Exception) {
            day // fallback to raw value
        }
    }

    fun displayTimeRange(): String {
        val inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())

        val startTime = try {
            rStart?.takeIf { it.lowercase() != "none" }?.let {
                LocalTime.parse(it, inputFormatter).format(outputFormatter)
            }
        } catch (e: Exception) {
            null
        }

        val endTime = try {
            rEnd?.takeIf { it.lowercase() != "none" }?.let {
                LocalTime.parse(it, inputFormatter).format(outputFormatter)
            }
        } catch (e: Exception) {
            null
        }

        return when {
            startTime != null && endTime != null -> "$startTime - $endTime"
            startTime != null -> "$startTime - N/A"
            endTime != null -> "N/A - $endTime"
            else -> "N/A"
        }
    }


}
