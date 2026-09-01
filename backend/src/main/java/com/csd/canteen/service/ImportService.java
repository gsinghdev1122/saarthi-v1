package com.csd.canteen.service;

import com.csd.canteen.dto.request.CreateImportRequest;
import com.csd.canteen.dto.response.ImportResponse;
import com.csd.canteen.entity.Activity;
import com.csd.canteen.entity.Employee;
import com.csd.canteen.entity.ImportBatch;
import com.csd.canteen.entity.InventoryItem;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.parser.CimsFileParser;
import com.csd.canteen.parser.FileParseException;
import com.csd.canteen.parser.ParsedAttendanceRow;
import com.csd.canteen.parser.ParsedInventoryRow;
import com.csd.canteen.repository.ActivityRepository;
import com.csd.canteen.repository.EmployeeRepository;
import com.csd.canteen.repository.ImportBatchRepository;
import com.csd.canteen.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImportService {

    private static final Set<String> PRN_FILE_TYPES = Set.of("inventory", "grocery_sales", "liquor_sales");
    private static final Set<String> ATTENDANCE_FILE_TYPES = Set.of("attendance", "payroll");
    private static final BigDecimal DEFAULT_REORDER_LEVEL = BigDecimal.valueOf(10);

    private final ImportBatchRepository importBatchRepository;
    private final ActivityRepository activityRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final EmployeeRepository employeeRepository;
    private final CanteenMapper mapper;
    private final CimsFileParser fileParser;

    @Transactional(readOnly = true)
    public List<ImportResponse> listRecent() {
        return importBatchRepository
                .findAllByOrderByUploadedAtDesc(PageRequest.of(0, 50, Sort.unsorted()))
                .stream().map(mapper::toResponse).toList();
    }

    /** Metadata-only registration (kept for callers that already know the row
     *  count and don't have a physical file to hand — e.g. a scripted import). */
    @Transactional
    public ImportResponse register(CreateImportRequest request) {
        ImportBatch saved = saveBatch(request.filename(), request.fileType(), request.canteen(),
                request.rowCount(), "processed", "File registered and parsed successfully");
        recordActivity(saved);
        return mapper.toResponse(saved);
    }

    /** Real upload path: actually parses the file's contents and writes the
     *  resulting inventory/attendance rows into the database, then registers
     *  the import batch with the true row count and any parse warnings. */
    @Transactional
    public ImportResponse registerAndParse(MultipartFile file, String fileType, String canteen) throws IOException {
        if (file.isEmpty()) {
            throw new FileParseException("Uploaded file is empty");
        }

        int rowCount;
        String message;

        if (PRN_FILE_TYPES.contains(fileType)) {
            List<ParsedInventoryRow> rows = fileParser.parseInventoryPrn(file.getInputStream());
            rows.forEach(row -> upsertInventoryItem(row, canteen));
            rowCount = rows.size();
            message = "Parsed and applied " + rowCount + " inventory rows";
        } else if (ATTENDANCE_FILE_TYPES.contains(fileType)) {
            List<ParsedAttendanceRow> rows = fileParser.parseAttendanceWorkbook(file.getInputStream(), file.getOriginalFilename());
            rows.forEach(row -> upsertAttendance(row, canteen));
            rowCount = rows.size();
            message = "Parsed and applied " + rowCount + " attendance rows";
        } else {
            throw new FileParseException("Unsupported fileType '" + fileType +
                    "'. Expected one of " + PRN_FILE_TYPES + " or " + ATTENDANCE_FILE_TYPES);
        }

        ImportBatch saved = saveBatch(file.getOriginalFilename(), fileType, canteen, rowCount, "processed", message);
        recordActivity(saved);
        return mapper.toResponse(saved);
    }

    private void upsertInventoryItem(ParsedInventoryRow row, String canteen) {
        InventoryItem item = inventoryItemRepository.findByIndexNoAndCanteen(row.indexNo(), canteen)
                .orElseGet(() -> InventoryItem.builder()
                        .indexNo(row.indexNo())
                        .canteen(canteen)
                        .reorderLevel(DEFAULT_REORDER_LEVEL.intValue())
                        .build());

        Integer previousStock = item.getClosingStock();
        item.setName(row.name());
        item.setDivision(row.division());
        item.setClosingStock(row.closingStock());
        item.setValue(row.value());
        item.setTrend(previousStock == null ? "stable"
                : row.closingStock() > previousStock ? "up"
                : row.closingStock() < previousStock ? "down" : "stable");
        item.setStatus(row.closingStock() <= item.getReorderLevel() ? "low" : "healthy");

        inventoryItemRepository.save(item);
    }

    private void upsertAttendance(ParsedAttendanceRow row, String canteen) {
        Employee employee = employeeRepository.findByEmployeeCode(row.employeeCode()).orElse(null);
        if (employee == null) {
            // Attendance files reference employees by code; if the employee
            // hasn't been registered via the Workforce screen yet, skip rather
            // than guess at their category/designation.
            return;
        }
        employee.setAttendance(row.attendancePercent());
        employeeRepository.save(employee);
    }

    private ImportBatch saveBatch(String filename, String fileType, String canteen, int rowCount, String status, String message) {
        ImportBatch batch = ImportBatch.builder()
                .filename(filename)
                .fileType(fileType)
                .canteen(canteen)
                .rowCount(rowCount)
                .status(status)
                .message(message)
                .build();
        return importBatchRepository.save(batch);
    }

    private void recordActivity(ImportBatch saved) {
        activityRepository.save(Activity.builder()
                .title("Source file processed")
                .detail(String.format(Locale.US, "%s \u00b7 %,d rows", saved.getFilename(), saved.getRowCount()))
                .kind("import")
                .build());
    }
}
