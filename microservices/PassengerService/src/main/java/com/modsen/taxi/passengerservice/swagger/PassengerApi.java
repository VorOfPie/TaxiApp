package com.modsen.taxi.passengerservice.swagger;

import com.modsen.taxi.passengerservice.dto.PassengerRequest;
import com.modsen.taxi.passengerservice.dto.PassengerResponse;
import com.modsen.taxi.passengerservice.dto.error.AppError;
import com.modsen.taxi.passengerservice.dto.error.AppErrorCustom;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Passenger Controller", description = "Passenger management API")
public interface PassengerApi {

    @Operation(summary = "Create a new passenger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Passenger created successfully", content = @Content(schema = @Schema(implementation = PassengerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<PassengerResponse>> createPassenger(@RequestBody @Valid PassengerRequest passengerRequest);

    @Operation(summary = "Update an existing passenger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger updated successfully", content = @Content(schema = @Schema(implementation = PassengerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<PassengerResponse>> updatePassenger(@PathVariable Long id, @RequestBody @Valid PassengerRequest passengerRequest);

    @Operation(summary = "Get passenger by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Passenger retrieved successfully", content = @Content(schema = @Schema(implementation = PassengerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Passenger not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<PassengerResponse>> getPassengerById(@PathVariable Long id);

    @Operation(summary = "Get all passengers with pagination and optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of passengers retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<Map<String, Object>>> getAllPassengers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort);

    @Operation(summary = "Delete a passenger by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Passenger deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<Void>> deletePassenger(@PathVariable Long id);
}
