package dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PassengerDTO(
        @NotNull(message = "ID cannot be null")
        Long id,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name must be less than 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 50, message = "Last name must be less than 50 characters")
        String lastName,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @NotBlank(message = "Phone cannot be blank")
        @Pattern(regexp = "\\+?[0-9\\-\\s]+", message = "Phone number should be valid")
        @Size(max = 20, message = "Phone number must be less than 20 characters")
        String phone
) {}