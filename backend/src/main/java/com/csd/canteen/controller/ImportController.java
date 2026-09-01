package com.csd.canteen.controller;

import com.csd.canteen.dto.request.CreateImportRequest;
import com.csd.canteen.dto.response.ImportResponse;
import com.csd.canteen.service.ImportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Tag(name = "Imports")
public class ImportController {

    private final ImportService importService;

    @GetMapping
    public List<ImportResponse> list() {
        return importService.listRecent();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CANTEEN_MANAGER','STORE_SUPERVISOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResponse register(@Valid @RequestBody CreateImportRequest request) {
        return importService.register(request);
    }

    /** Real file upload: parses a CIMS fixed-width .prn export or an Excel
     *  .xls/.xlsx export and persists the actual inventory/employee rows it
     *  contains, in addition to registering the import batch. */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','CANTEEN_MANAGER','STORE_SUPERVISOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType,
            @RequestParam("canteen") String canteen) throws IOException {
        return importService.registerAndParse(file, fileType, canteen);
    }
}
