package com.csd.canteen.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DashboardSummaryResponse(
        String canteen,
        BigDecimal salesToday,
        BigDecimal inventoryValue,
        long lowStockItems,
        long pendingApprovals,
        BigDecimal attendanceRate,
        OffsetDateTime lastImport
) {}
