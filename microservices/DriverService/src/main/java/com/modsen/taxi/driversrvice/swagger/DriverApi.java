package com.modsen.taxi.driversrvice.swagger;

import com.modsen.taxi.driversrvice.dto.error.AppError;
import com.modsen.taxi.driversrvice.dto.error.AppErrorCustom;
import com.modsen.taxi.driversrvice.dto.request.DriverRequest;
import com.modsen.taxi.driversrvice.dto.response.DriverResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Driver Controller", description = "API for managing drivers")
public interface DriverApi {

    @Operation(summary = "Get a list of drivers with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of drivers retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<Map<String, Object>>> getAllDrivers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort);

    @Operation(summary = "Create a new driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver created successfully", content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<DriverResponse>> createDriver(@Validated @RequestBody DriverRequest driverRequest);

    @Operation(summary = "Update an existing driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver updated successfully", content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<DriverResponse>> updateDriver(@PathVariable Long id, @Validated @RequestBody DriverRequest driverRequest);

    @Operation(summary = "Delete a driver by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Driver deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<Void>> deleteDriver(@PathVariable Long id);

    @Operation(summary = "Get driver details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver retrieved successfully", content = @Content(schema = @Schema(implementation = DriverResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<DriverResponse>> getDriverById(@PathVariable Long id);
}
