package com.example.ses

import java.time.LocalDateTime
import java.util.UUID

data class CalendarRequest(
    val uid: String = UUID.randomUUID().toString(),
    val fromEmail: String,
    val toEmail: String,
    val subject: String,
    val body: String,
    val meetingStartTime: LocalDateTime,
    val meetingEndTime: LocalDateTime,
    val location: String,
    val organizerName: String
)