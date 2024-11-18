package com.modsen.taxi.passengerservice.dto.error

import java.time.LocalDateTime

data class AppError(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime
)
