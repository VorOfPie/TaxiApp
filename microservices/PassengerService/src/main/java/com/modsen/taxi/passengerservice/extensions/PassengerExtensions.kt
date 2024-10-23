package com.modsen.taxi.passengerservice.extensions

import com.modsen.taxi.passengerservice.domain.Passenger
import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse

fun PassengerRequest.toPassenger(): Passenger {
    return Passenger(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        isDeleted = false
    )
}

fun Passenger.toResponse(): PassengerResponse {
    return PassengerResponse(
        id = this.id!!,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
    )
}

fun PassengerRequest.updatePassenger(passenger: Passenger) {
    passenger.firstName = this.firstName
    passenger.lastName = this.lastName
    passenger.email = this.email
    passenger.phone = this.phone
}
