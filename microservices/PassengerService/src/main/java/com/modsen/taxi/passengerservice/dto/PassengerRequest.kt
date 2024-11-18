package com.modsen.taxi.passengerservice.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PassengerRequest(
    @field:NotBlank(message = "First name cannot be blank")
    @field:Size(max = 50, message = "First name must be less than 50 characters")
    val firstName: String,

    @field:NotBlank(message = "Last name cannot be blank")
    @field:Size(max = 50, message = "Last name must be less than 50 characters")
    val lastName: String,

    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email should be valid")
    @field:Size(max = 100, message = "Email must be less than 100 characters")
    val email: String,

    @field:NotBlank(message = "Phone cannot be blank")
    @field:Pattern(regexp = "\\+?[0-9\\-\\s]+", message = "Phone number should be valid")
    @field:Size(max = 20, message = "Phone number must be less than 20 characters")
    val phone: String
)
