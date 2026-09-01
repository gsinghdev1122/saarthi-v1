package com.csd.canteen.service;

import com.csd.canteen.dto.request.CreateExpenseRequest;
import com.csd.canteen.dto.response.ExpenseResponse;
import com.csd.canteen.entity.Approval;
import com.csd.canteen.entity.Expense;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.ApprovalRepository;
import com.csd.canteen.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private ApprovalRepository approvalRepository;
    private final CanteenMapper mapper = new CanteenMapper();
    private ExpenseService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseService(expenseRepository, approvalRepository, mapper);
    }

    @Test
    void creatingAnExpenseAlsoRaisesAnApproval() {
        CreateExpenseRequest request = new CreateExpenseRequest("Repairs", "Acme Co.", new BigDecimal("5000.00"), LocalDate.now());
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setId(42L);
            return e;
        });
        when(approvalRepository.save(any(Approval.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse response = service.create(request);

        assertThat(response.status()).isEqualTo("draft");
        assertThat(response.amount()).isEqualByComparingTo("5000.00");

        ArgumentCaptor<Approval> approvalCaptor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalRepository).save(approvalCaptor.capture());
        assertThat(approvalCaptor.getValue().getStatus()).isEqualTo("pending");
        assertThat(approvalCaptor.getValue().getAmount()).isEqualByComparingTo("5000.00");
    }
}
