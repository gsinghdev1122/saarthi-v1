package com.csd.canteen.service;

import com.csd.canteen.dto.response.DashboardSummaryResponse;
import com.csd.canteen.entity.Employee;
import com.csd.canteen.entity.ImportBatch;
import com.csd.canteen.entity.InventoryItem;
import com.csd.canteen.repository.ApprovalRepository;
import com.csd.canteen.repository.EmployeeRepository;
import com.csd.canteen.repository.ImportBatchRepository;
import com.csd.canteen.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String DEFAULT_CANTEEN = "Delhi Cantt";
    // Placeholder until the real sales-import pipeline (see docs/BUILD-AND-DEPLOY.md) is wired up.
    private static final BigDecimal PLACEHOLDER_SALES_TODAY = new BigDecimal("284650");

    private final InventoryItemRepository inventoryItemRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalRepository approvalRepository;
    private final ImportBatchRepository importBatchRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        List<InventoryItem> inventory = inventoryItemRepository.findAll();
        List<Employee> employees = employeeRepository.findAll();
        long lowStock = inventoryItemRepository.countByStatus("low");
        long pendingApprovals = approvalRepository.countByStatus("pending");
        ImportBatch lastImport = importBatchRepository.findFirstByOrderByUploadedAtDesc();

        BigDecimal inventoryValue = inventory.stream()
                .map(InventoryItem::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal attendanceRate = employees.isEmpty()
                ? new BigDecimal("96.4")
                : employees.stream().map(Employee::getAttendance).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);

        return new DashboardSummaryResponse(
                DEFAULT_CANTEEN,
                PLACEHOLDER_SALES_TODAY,
                inventoryValue,
                lowStock,
                pendingApprovals,
                attendanceRate,
                lastImport != null ? lastImport.getUploadedAt() : null
        );
    }
}
