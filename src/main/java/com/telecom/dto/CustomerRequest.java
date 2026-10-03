package com.telecom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Its Bean Validation annotations also become the "Constraints" column in the docs. */
public record CustomerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") String msisdn,
        @NotBlank @Size(max = 20) String planCode) {
}
