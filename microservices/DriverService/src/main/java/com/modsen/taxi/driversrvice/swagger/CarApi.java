package com.modsen.taxi.driversrvice.swagger;

import com.modsen.taxi.driversrvice.dto.error.AppError;
import com.modsen.taxi.driversrvice.dto.error.AppErrorCustom;
import com.modsen.taxi.driversrvice.dto.request.CreateCarRequest;
import com.modsen.taxi.driversrvice.dto.response.CarResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Car Controller", description = """
            API for managing cars, including creation, updating, deletion, and retrieval of car details. 
            Access is controlled through role-based security:
            - Users with the role `ROLE_USER` can access and modify their own car details.
            - Users with the role `ROLE_ADMIN` have full access to all cars.
            Each request must include a valid JWT token in the Authorization header (Bearer Authentication).
        """)
public interface CarApi {

    @Operation(
            summary = "Create a new car",
            description = "Allows an admin or authorized user to create a new car record in the system.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Car created successfully", content = @Content(schema = @Schema(implementation = CarResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<CarResponse>> createCar(@RequestBody CreateCarRequest createCarRequest);

    @Operation(summary = "Update an existing car")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car updated successfully", content = @Content(schema = @Schema(implementation = CarResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Car not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<CarResponse>> updateCar(@PathVariable Long id,
                                                @RequestBody CreateCarRequest createCarRequest,
                                                @AuthenticationPrincipal Jwt jwt);

    @Operation(
            summary = "Get car details by ID",
            description = "Retrieves car details by its ID. Access is restricted to the car owner or admins.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car retrieved successfully", content = @Content(schema = @Schema(implementation = CarResponse.class))),
            @ApiResponse(responseCode = "404", description = "Car not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<CarResponse>> getCarById(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt);

    @Operation(
            summary = "Get a list of cars with optional filters",
            description = """
                        Retrieves a paginated list of cars with optional filters (brand, color, license plate, and active status).
                        This endpoint is restricted to admin users.
                    """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of cars retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<Map<String, Object>>> getAllCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(defaultValue = "true") boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort);

    @Operation(
            summary = "Delete a car by ID",
            description = "Marks a car as deleted. Restricted to the car owner or admins.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Car deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Car not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    Mono<ResponseEntity<Void>> deleteCar(@PathVariable Long id,
                                         @AuthenticationPrincipal Jwt jwt);
}
