package com.modsen.taxi.passengerservice.controller

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.service.PassengerService
import com.modsen.taxi.passengerservice.swagger.PassengerApi
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

data class PassengerController(
    private val passengerService: PassengerService
) : PassengerApi {

    @PostMapping
    override fun createPassenger(
        @Valid @RequestBody passengerRequest: PassengerRequest
    ): Mono<ResponseEntity<PassengerResponse>> =
        passengerService.createPassenger(passengerRequest)
            .map { passenger -> ResponseEntity(passenger, HttpStatus.CREATED) }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    override fun updatePassenger(
        @PathVariable id: Long,
        @Valid @RequestBody passengerRequest: PassengerRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<PassengerResponse>> {
        val principalEmail = jwt.getClaim<String>("email")
        val isAdmin = isAdmin(jwt)
        return passengerService.updatePassenger(id, passengerRequest, principalEmail, isAdmin)
            .map { updatedPassenger -> ResponseEntity(updatedPassenger, HttpStatus.OK) }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    override fun getPassengerById(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<PassengerResponse>> {
        val principalEmail = jwt.getClaim<String>("email")
        val isAdmin = isAdmin(jwt)
        return passengerService.getPassengerById(id, principalEmail, isAdmin)
            .map { passenger -> ResponseEntity(passenger, HttpStatus.OK) }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    override fun getAllPassengers(
        @RequestParam(required = false) firstName: String?,
        @RequestParam(required = false) lastName: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(defaultValue = "true") isActive: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id,asc") sort: String
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val sortParams = sort.split(",")
        var sortOrder = Sort.by(sortParams[0]).ascending()
        if ("desc".equals(sortParams[1], ignoreCase = true)) {
            sortOrder = Sort.by(sortParams[0]).descending()
        }
        val pageable: Pageable = PageRequest.of(page, size, sortOrder)

        return passengerService.getAllPassengers(pageable, firstName, lastName, email, isActive)
            .map { pagePassengers ->
                val response: Map<String, Any> = mapOf(
                    "passengers" to pagePassengers.content,
                    "currentPage" to pagePassengers.number,
                    "totalItems" to pagePassengers.totalElements,
                    "totalPages" to pagePassengers.totalPages
                )
                ResponseEntity(response, HttpStatus.OK)
            }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    override fun deletePassenger(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<Void>> {
        val principalEmail = jwt.getClaim<String>("email")
        val isAdmin = isAdmin(jwt)
        return passengerService.deletePassenger(id, principalEmail, isAdmin)
            .thenReturn(ResponseEntity<Void>(HttpStatus.NO_CONTENT))
    }

    private fun isAdmin(jwt: Jwt): Boolean {
        val roles = jwt.getClaim<Map<String, Any>>("realm_access")?.get("roles") as? List<String>
        return roles?.contains("ROLE_ADMIN") ?: false
    }
}
