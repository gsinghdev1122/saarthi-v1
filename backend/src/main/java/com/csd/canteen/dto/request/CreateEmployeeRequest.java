package com.csd.canteen.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "employeeCode is required") String employeeCode,
        @NotBlank(message = "category is required") String category,
        @NotBlank(message = "designation is required") String designation,
        LocalDate contractEnd
) {}
