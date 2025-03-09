package com.modsen.taxi.passengerservice.swagger

import com.modsen.taxi.passengerservice.dto.PassengerRequest
import com.modsen.taxi.passengerservice.dto.PassengerResponse
import com.modsen.taxi.passengerservice.dto.error.AppError
import com.modsen.taxi.passengerservice.dto.error.AppErrorCustom
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

@Tag(
    name = "Passenger Controller", description = """
            API for managing passengers, including creation, updating, deletion, and retrieval of passenger details.
            Access is controlled through role-based security:
            - Users with the role `ROLE_USER` can access and modify their own passenger details.
            - Users with the role `ROLE_ADMIN` have full access to all passengers.
            Each request must include a valid JWT token in the Authorization header (Bearer Authentication).
        """
)
interface PassengerApi {

    @Operation(
        summary = "Create a new passenger",
        description = "Allows an admin or authorized user to create a new passenger record in the system.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Passenger created successfully",
                content = [Content(schema = Schema(implementation = PassengerResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = [Content(schema = Schema(implementation = AppErrorCustom::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = AppError::class))]
            )
        ]
    )
    fun createPassenger(@RequestBody @Valid passengerRequest: PassengerRequest): Mono<ResponseEntity<PassengerResponse>>

    @Operation(
        summary = "Update an existing passenger",
        description = "Allows an admin or user to update an existing passenger's details.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Passenger updated successfully",
                content = [Content(schema = Schema(implementation = PassengerResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = [Content(schema = Schema(implementation = AppErrorCustom::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Passenger not found",
                content = [Content(schema = Schema(implementation = AppError::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = AppError::class))]
            )
        ]
    )
    fun updatePassenger(
        @PathVariable id: Long,
        @RequestBody @Valid passengerRequest: PassengerRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<PassengerResponse>>

    @Operation(
        summary = "Get passenger details by ID",
        description = "Retrieves passenger details by their ID. Access is restricted to the passenger owner or admins.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Passenger retrieved successfully",
                content = [Content(schema = Schema(implementation = PassengerResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Passenger not found",
                content = [Content(schema = Schema(implementation = AppError::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = AppError::class))]
            )
        ]
    )
    fun getPassengerById(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): Mono<ResponseEntity<PassengerResponse>>

    @Operation(
        summary = "Get a list of passengers with optional filters",
        description = """
            Retrieves a paginated list of passengers with optional filters (first name, last name, email, active status).
            This endpoint is restricted to admin users.
        """,
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of passengers retrieved successfully",
                content = [Content(schema = Schema(implementation = Map::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid filter parameters",
                content = [Content(schema = Schema(implementation = AppErrorCustom::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = AppError::class))]
            )
        ]
    )
    fun getAllPassengers(
        @RequestParam(required = false) firstName: String?,
        @RequestParam(required = false) lastName: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(defaultValue = "true") isActive: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id,asc") sort: String
    ): Mono<ResponseEntity<Map<String, Any>>>

    @Operation(
        summary = "Delete a passenger by ID",
        description = "Marks a passenger as deleted. Restricted to the passenger owner or admins.",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Passenger deleted successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Passenger not found",
                content = [Content(schema = Schema(implementation = AppError::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = AppError::class))]
            )
        ]
    )
    fun deletePassenger(@PathVariable id: Long, @AuthenticationPrincipal jwt: Jwt): Mono<ResponseEntity<Void>>
}
