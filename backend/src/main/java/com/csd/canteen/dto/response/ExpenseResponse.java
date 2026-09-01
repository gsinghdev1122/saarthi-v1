package com.csd.canteen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id, String category, String vendor, BigDecimal amount,
        LocalDate date, String status, String submittedBy, String canteen
) {}
