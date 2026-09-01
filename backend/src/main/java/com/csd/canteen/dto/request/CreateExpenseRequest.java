package com.csd.canteen.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotBlank(message = "category is required") String category,
        @NotBlank(message = "vendor is required") String vendor,
        @NotNull @DecimalMin(value = "0.0", inclusive = true, message = "amount cannot be negative") BigDecimal amount,
        @NotNull LocalDate date
) {}
