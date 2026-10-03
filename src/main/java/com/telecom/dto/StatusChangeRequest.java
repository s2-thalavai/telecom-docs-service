package com.telecom.dto;

import com.telecom.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusChangeRequest(@NotNull CustomerStatus status, @Size(max = 255) String reason) {
}
