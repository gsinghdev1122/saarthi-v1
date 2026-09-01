package com.csd.canteen.dto.response;

import java.time.OffsetDateTime;

public record ActivityResponse(
        Long id, String title, String detail, String kind, OffsetDateTime timestamp
) {}
