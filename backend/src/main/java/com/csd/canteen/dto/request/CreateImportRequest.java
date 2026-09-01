package com.csd.canteen.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateImportRequest(
        @NotBlank(message = "filename is required") String filename,
        @NotBlank(message = "fileType is required") String fileType,
        @NotBlank(message = "canteen is required") String canteen,
        @NotNull @Min(value = 0, message = "rowCount cannot be negative") Integer rowCount
) {}
