package com.csd.canteen.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ReportsOverviewResponse(
        BigDecimal sales, BigDecimal expenses, BigDecimal profit, List<SalesByMonth> salesByMonth
) {}
