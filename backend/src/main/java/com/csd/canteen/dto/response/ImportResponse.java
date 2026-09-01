package com.csd.canteen.dto.response;

import java.time.OffsetDateTime;

public record ImportResponse(
        Long id, String filename, String fileType, String canteen,
        String status, Integer rowCount, String message, OffsetDateTime uploadedAt
) {}
