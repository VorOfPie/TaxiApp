package com.modsen.taxi.passengerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PassengerServiceApplication

fun main(args: Array<String>) {
    runApplication<PassengerServiceApplication>(*args)
}
