package com.modsen.taxi.passengerservice.dto

data class PassengerResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String
)
