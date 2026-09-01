package com.csd.canteen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id, String name, String employeeCode, String category, String designation,
        BigDecimal attendance, LocalDate contractEnd, String status, String canteen
) {}
