package com.csd.canteen.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ApprovalResponse(
        Long id, String type, String reference, BigDecimal amount,
        String submittedBy, OffsetDateTime submittedAt, String status
) {}
