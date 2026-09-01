package com.csd.canteen.dto.response;

import java.math.BigDecimal;

public record SalesByMonth(String month, BigDecimal sales, BigDecimal expenses) {}
