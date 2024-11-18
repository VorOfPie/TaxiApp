package com.modsen.taxi.passengerservice.controller

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.service.PassengerService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/passengers")
open class PassengerController(private val passengerService: PassengerService) {

    @PostMapping
    fun createPassenger(@Valid @RequestBody passengerRequest: PassengerRequest): Mono<ResponseEntity<PassengerResponse>> {
        return passengerService.createPassenger(passengerRequest)
            .map { passenger -> ResponseEntity(passenger, HttpStatus.CREATED) }
    }

    @PutMapping("/{id}")
    fun updatePassenger(@PathVariable id: Long, @Valid @RequestBody passengerRequest: PassengerRequest): Mono<ResponseEntity<PassengerResponse>> {
        return passengerService.updatePassenger(id, passengerRequest)
            .map { updatedPassenger -> ResponseEntity(updatedPassenger, HttpStatus.OK) }
    }

    @GetMapping("/{id}")
    fun getPassengerById(@PathVariable id: Long): Mono<ResponseEntity<PassengerResponse>> {
        return passengerService.getPassengerById(id)
            .map { passenger -> ResponseEntity(passenger, HttpStatus.OK) }
    }

    @GetMapping
    fun getAllPassengers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) firstName: String? = null,
        @RequestParam(required = false) lastName: String? = null,
        @RequestParam(required = false) email: String? = null,
        @RequestParam(defaultValue = "true") isActive: Boolean? = null,
        @RequestParam(required = false) sort: String? = null
    ): Mono<ResponseEntity<Map<String, Any>>> {

        val sortOrder = if (sort != null) {
            Sort.by(sort.split(",")[0]).let {
                if (sort.split(",").getOrNull(1)?.equals("desc", ignoreCase = true) == true) it.descending() else it.ascending()
            }
        } else {
            Sort.unsorted()
        }

        val pageable: Pageable = PageRequest.of(page, size, sortOrder)

        return passengerService.getAllPassengers(pageable, firstName, lastName, email, isActive)
            .map { pagePassengers ->
                val response = mapOf(
                    "passengers" to pagePassengers.content,
                    "currentPage" to pagePassengers.number,
                    "totalItems" to pagePassengers.totalElements,
                    "totalPages" to pagePassengers.totalPages
                )
                ResponseEntity(response, HttpStatus.OK)
            }
    }


    @DeleteMapping("/{id}")
    fun deletePassenger(@PathVariable id: Long): Mono<ResponseEntity<Void>> {
        return passengerService.deletePassenger(id)
            .then(Mono.just(ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
    }
}
