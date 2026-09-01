package com.csd.canteen.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the two real CIMS export formats this canteen system receives:
 *
 *  1. Fixed-width ".prn" inventory/sales exports (legacy DOS-era CIMS print format).
 *  2. Excel ".xls"/".xlsx" biometric attendance exports.
 *
 * COLUMN LAYOUT NOTE: the exact byte offsets of the real CIMS .prn format and
 * the exact header names of the real biometric .xls export are site-specific
 * and weren't available at build time. The layouts below are a documented,
 * sensible default (and are each isolated in one place) — adjust
 * {@link #INVENTORY_COLUMNS} or the header names in
 * {@link #parseAttendanceWorkbook} to match your actual CIMS output once you
 * have a real sample file, without touching any other part of the app.
 */
@Component
@Slf4j
public class CimsFileParser {

    // --- Fixed-width layout for inventory/sales .prn files ---
    // [start, end) character offsets per line.
    private static final int[] INDEX_NO      = {0, 12};
    private static final int[] NAME          = {12, 42};
    private static final int[] DIVISION      = {42, 52};
    private static final int[] CLOSING_STOCK = {52, 62};
    private static final int[] VALUE         = {62, 78};
    private static final int MIN_LINE_LENGTH = 78;

    /** Documents the layout above in one exportable place (e.g. for an admin help screen). */
    public static final String INVENTORY_COLUMNS =
            "IndexNo[0-12) Name[12-42) Division[42-52) ClosingStock[52-62) Value[62-78)";

    public List<ParsedInventoryRow> parseInventoryPrn(InputStream inputStream) {
        List<ParsedInventoryRow> rows = new ArrayList<>();
        int lineNo = 0;
        int skipped = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank() || line.length() < MIN_LINE_LENGTH) {
                    skipped++;
                    continue;
                }
                try {
                    String indexNo = slice(line, INDEX_NO);
                    String name = slice(line, NAME);
                    String division = slice(line, DIVISION);
                    if (indexNo.isEmpty() || name.isEmpty()) {
                        skipped++;
                        continue;
                    }
                    Integer closingStock = parseIntOrNull(slice(line, CLOSING_STOCK));
                    BigDecimal value = parseDecimalOrNull(slice(line, VALUE));
                    if (closingStock == null || value == null) {
                        skipped++;
                        continue;
                    }
                    rows.add(new ParsedInventoryRow(indexNo, name, normalizeDivision(division), closingStock, value));
                } catch (Exception rowError) {
                    log.warn("Skipping unparseable .prn line {}: {}", lineNo, rowError.getMessage());
                    skipped++;
                }
            }
        } catch (IOException e) {
            throw new FileParseException("Could not read the uploaded .prn file", e);
        }
        if (rows.isEmpty()) {
            throw new FileParseException("No valid inventory rows found. Expected fixed-width columns: " + INVENTORY_COLUMNS);
        }
        log.info("Parsed {} inventory rows from .prn file ({} lines skipped)", rows.size(), skipped);
        return rows;
    }

    // --- Excel layout for attendance .xls/.xlsx files ---
    // Expected header row (case-insensitive, any order): EmployeeCode, Name,
    // Category, Designation, DaysPresent, TotalDays
    public List<ParsedAttendanceRow> parseAttendanceWorkbook(InputStream inputStream, String originalFilename) {
        List<ParsedAttendanceRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new FileParseException("Attendance workbook has no data rows");
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            ColumnMap columns = ColumnMap.from(headerRow);

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String employeeCode = readString(row, columns.employeeCode);
                if (employeeCode == null || employeeCode.isBlank()) continue;

                String name = readString(row, columns.name);
                String category = readString(row, columns.category);
                String designation = readString(row, columns.designation);
                BigDecimal daysPresent = readNumeric(row, columns.daysPresent);
                BigDecimal totalDays = readNumeric(row, columns.totalDays);

                if (daysPresent == null || totalDays == null || totalDays.compareTo(BigDecimal.ZERO) == 0) {
                    log.warn("Skipping attendance row {} for '{}': missing/zero days", r + 1, employeeCode);
                    continue;
                }

                BigDecimal attendancePercent = daysPresent
                        .divide(totalDays, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                rows.add(new ParsedAttendanceRow(employeeCode.trim(), name, category, designation, attendancePercent));
            }
        } catch (FileParseException e) {
            throw e;
        } catch (Exception e) {
            throw new FileParseException("Could not read '" + originalFilename + "' as an Excel attendance export", e);
        }
        if (rows.isEmpty()) {
            throw new FileParseException("No valid attendance rows found in the workbook");
        }
        log.info("Parsed {} attendance rows from {}", rows.size(), originalFilename);
        return rows;
    }

    private String slice(String line, int[] range) {
        int end = Math.min(range[1], line.length());
        if (range[0] >= end) return "";
        return line.substring(range[0], end).trim();
    }

    private Integer parseIntOrNull(String s) {
        try {
            return s.isBlank() ? null : Integer.parseInt(s.replaceAll("[,\\s]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimalOrNull(String s) {
        try {
            return s.isBlank() ? null : new BigDecimal(s.replaceAll("[,\\s]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeDivision(String raw) {
        String d = raw.trim();
        if (d.equalsIgnoreCase("GRO") || d.equalsIgnoreCase("GROCERY")) return "Grocery";
        if (d.equalsIgnoreCase("LIQ") || d.equalsIgnoreCase("LIQUOR")) return "Liquor";
        return d.isEmpty() ? "Grocery" : d;
    }

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private String readString(Row row, int colIndex) {
        if (colIndex < 0) return null;
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        // DataFormatter reads the cell's displayed value regardless of its
        // underlying type (numeric/string/formula), which is more robust than
        // asserting a type on cells coming from an externally-produced export.
        String value = DATA_FORMATTER.formatCellValue(cell);
        return value.isBlank() ? null : value.trim();
    }

    private BigDecimal readNumeric(Row row, int colIndex) {
        if (colIndex < 0) return null;
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            return new BigDecimal(cell.getStringCellValue().trim());
        } catch (Exception e) {
            return null;
        }
    }

    /** Resolves header names to column indices, case-insensitively, so the sheet's
     *  column order doesn't matter. */
    private static class ColumnMap {
        int employeeCode = -1, name = -1, category = -1, designation = -1, daysPresent = -1, totalDays = -1;

        static ColumnMap from(Row headerRow) {
            ColumnMap map = new ColumnMap();
            if (headerRow == null) throw new FileParseException("Attendance workbook is missing a header row");
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue().trim().toLowerCase().replaceAll("[\\s_]", "");
                int idx = cell.getColumnIndex();
                switch (header) {
                    case "employeecode", "empcode", "code" -> map.employeeCode = idx;
                    case "name", "employeename" -> map.name = idx;
                    case "category" -> map.category = idx;
                    case "designation" -> map.designation = idx;
                    case "dayspresent", "present" -> map.daysPresent = idx;
                    case "totaldays", "total" -> map.totalDays = idx;
                    default -> { /* ignore unknown columns */ }
                }
            }
            if (map.employeeCode < 0 || map.daysPresent < 0 || map.totalDays < 0) {
                throw new FileParseException(
                        "Attendance workbook header must include EmployeeCode, DaysPresent and TotalDays columns");
            }
            return map;
        }
    }
}
