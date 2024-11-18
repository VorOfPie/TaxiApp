package com.modsen.taxi.passengerservice.domain

import jakarta.persistence.*

@Entity
@Table(name = "passengers")
data class Passenger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var firstName: String,

    var lastName: String,

    var email: String,

    var phone: String,

    var isDeleted: Boolean = false
) {
    constructor() : this(null, "", "", "", "", false)
}
