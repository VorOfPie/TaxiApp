package com.modsen.taxi.ratingservice.swagger;

import com.modsen.taxi.ratingservice.dto.RatingRequest;
import com.modsen.taxi.ratingservice.dto.error.AppError;
import com.modsen.taxi.ratingservice.dto.error.AppErrorCustom;
import com.modsen.taxi.ratingservice.dto.response.RatingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(
        name = "Rating Controller",
        description = """
        API for managing ratings, including creation, updating, retrieval, and deletion of ratings.
        - Access controlled by roles:
          - `ROLE_USER`: Allows users to create, update, and view their own ratings.
          - `ROLE_ADMIN`: Full access to all ratings and the ability to perform any action on any rating.
        - All requests require a valid JWT token in the Authorization header (Bearer Authentication).
    """
)
public interface RatingApi {

    @Operation(
            summary = "Create a new rating",
            description = "Allows a user or admin to create a new rating for a driver or passenger.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rating created successfully", content = @Content(schema = @Schema(implementation = RatingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<RatingResponse> createRating(@RequestBody @Valid RatingRequest ratingRequest);

    @Operation(
            summary = "Update an existing rating",
            description = "Allows a user or admin to update an existing rating.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating updated successfully", content = @Content(schema = @Schema(implementation = RatingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "404", description = "Rating not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<RatingResponse> updateRating(@PathVariable Long id, @RequestBody @Valid RatingRequest ratingRequest);

    @Operation(
            summary = "Get rating details by ID",
            description = "Retrieves a rating by its ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating retrieved successfully", content = @Content(schema = @Schema(implementation = RatingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rating not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<RatingResponse> getRatingById(@PathVariable Long id);

    @Operation(
            summary = "Get a list of ratings with optional filters",
            description = """
            Retrieves a paginated list of ratings, with optional filters for driver and passenger IDs.
            Admin role is required to access the list of ratings for all users.
        """,
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of ratings retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters", content = @Content(schema = @Schema(implementation = AppErrorCustom.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<Map<String, Object>> getAllRatings(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort);

    @Operation(
            summary = "Get the average rating for a specific driver",
            description = "Retrieves the average rating for a driver by their driver ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average rating retrieved successfully", content = @Content(schema = @Schema(implementation = Double.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<Double> getAverageRatingForDriver(@PathVariable Long driverId);

    @Operation(
            summary = "Delete a rating by ID",
            description = "Marks a rating as deleted or removes it from the system.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rating deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rating not found", content = @Content(schema = @Schema(implementation = AppError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = AppError.class)))
    })
    ResponseEntity<Void> deleteRating(@PathVariable Long id);
}
