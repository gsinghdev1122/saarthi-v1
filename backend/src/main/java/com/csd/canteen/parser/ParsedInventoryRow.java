package com.csd.canteen.parser;

import java.math.BigDecimal;

public record ParsedInventoryRow(
        String indexNo, String name, String division, Integer closingStock, BigDecimal value
) {}
