package com.csd.canteen.mapper;

import com.csd.canteen.dto.response.*;
import com.csd.canteen.entity.*;
import org.springframework.stereotype.Component;

/** Central place converting JPA entities into API response DTOs. Kept explicit (no MapStruct)
 *  so behaviour is easy to trace and unit test without annotation-processor magic. */
@Component
public class CanteenMapper {

    public ImportResponse toResponse(ImportBatch e) {
        return new ImportResponse(e.getId(), e.getFilename(), e.getFileType(), e.getCanteen(),
                e.getStatus(), e.getRowCount(), e.getMessage(), e.getUploadedAt());
    }

    public InventoryItemResponse toResponse(InventoryItem e) {
        return new InventoryItemResponse(e.getId(), e.getIndexNo(), e.getName(), e.getDivision(),
                e.getClosingStock(), e.getReorderLevel(), e.getValue(), e.getTrend(), e.getStatus(), e.getCanteen());
    }

    public EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmployeeCode(), e.getCategory(),
                e.getDesignation(), e.getAttendance(), e.getContractEnd(), e.getStatus(), e.getCanteen());
    }

    public ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getCategory(), e.getVendor(), e.getAmount(),
                e.getDate(), e.getStatus(), e.getSubmittedBy(), e.getCanteen());
    }

    public ApprovalResponse toResponse(Approval e) {
        return new ApprovalResponse(e.getId(), e.getType(), e.getReference(), e.getAmount(),
                e.getSubmittedBy(), e.getSubmittedAt(), e.getStatus());
    }

    public ActivityResponse toResponse(Activity e) {
        return new ActivityResponse(e.getId(), e.getTitle(), e.getDetail(), e.getKind(), e.getTimestamp());
    }
}
