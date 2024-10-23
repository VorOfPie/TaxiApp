package com.modsen.taxi.passengerservice.dto.error

import java.time.LocalDateTime

data class AppErrorCustom(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime,
    val errors: Map<String, String>
)
