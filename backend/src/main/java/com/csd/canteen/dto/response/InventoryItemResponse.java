package com.csd.canteen.dto.response;

import java.math.BigDecimal;

public record InventoryItemResponse(
        Long id, String indexNo, String name, String division,
        Integer closingStock, Integer reorderLevel, BigDecimal value,
        String trend, String status, String canteen
) {}
