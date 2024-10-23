package com.modsen.taxi.passengerservice.repository

import com.modsen.taxi.passengerservice.domain.Passenger
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PassengerRepository : JpaRepository<Passenger?, Long?> {
    fun findByIdAndIsDeletedFalse(id: Long?): Optional<Passenger?>?
    fun findAllByIsDeletedFalse(): List<Passenger?>?
    fun existsByEmail(email: String?): Boolean
    fun findByEmailAndIsDeletedFalse(email: String?): Optional<Passenger?>?
    fun deletePassengerByEmail(email: String?)
    fun deleteByEmail(email: String?)
}