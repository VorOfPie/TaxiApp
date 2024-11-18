package com.modsen.taxi.tripservice.swagger;

import com.modsen.taxi.tripservice.dto.error.AppError;
import com.modsen.taxi.tripservice.dto.error.AppErrorCustom;
import com.modsen.taxi.tripservice.dto.request.ScoreRequest;
import com.modsen.taxi.tripservice.dto.request.TripRequest;
import com.modsen.taxi.tripservice.dto.response.TripResponse;
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

import java.util.Map;

@Tag(name = "Trip Controller", description = "Trip management API")
public interface TripApi {

    @Operation(summary = "Create a new trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trip created successfully", content = @Content(schema = @Schema(implementation = TripResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<TripResponse> createTrip(@RequestBody @Valid TripRequest tripRequest);

    @Operation(summary = "Update an existing trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip updated successfully", content = @Content(schema = @Schema(implementation = TripResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<TripResponse> updateTrip(@PathVariable Long id, @RequestBody @Valid TripRequest tripRequest);

    @Operation(summary = "Update the status of an existing trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip status updated successfully", content = @Content(schema = @Schema(implementation = TripResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<TripResponse> updateTripStatus(@PathVariable Long id, @RequestParam String status);

    @Operation(summary = "Get trip details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip retrieved successfully", content = @Content(schema = @Schema(implementation = TripResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<TripResponse> getTripById(@PathVariable Long id);

    @Operation(summary = "Get a list of trips with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of trips retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<Map<String, Object>> getAllTrips(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(required = false) String originAddress,
            @RequestParam(required = false) String destinationAddress,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort);

    @Operation(summary = "Delete a trip by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trip deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<Void> deleteTrip(@PathVariable Long id);

    @Operation(summary = "Close a trip and provide a rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip closed and rated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rating data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<Void> closeTrip(@PathVariable Long id, @RequestBody @Valid ScoreRequest scoreRequest);
}
