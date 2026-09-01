package com.csd.canteen.parser;

import java.math.BigDecimal;

public record ParsedAttendanceRow(
        String employeeCode, String name, String category, String designation, BigDecimal attendancePercent
) {}
