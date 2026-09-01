package com.csd.canteen.service;

import com.csd.canteen.dto.request.CreateExpenseRequest;
import com.csd.canteen.dto.response.ExpenseResponse;
import com.csd.canteen.entity.Approval;
import com.csd.canteen.entity.Expense;
import com.csd.canteen.mapper.CanteenMapper;
import com.csd.canteen.repository.ApprovalRepository;
import com.csd.canteen.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ApprovalRepository approvalRepository;
    private final CanteenMapper mapper;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listAll() {
        return expenseRepository.findAllByOrderByDateDesc().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        Expense expense = Expense.builder()
                .category(request.category())
                .vendor(request.vendor())
                .amount(request.amount())
                .date(request.date())
                .status("draft")
                .submittedBy("Canteen Manager")
                .build();
        Expense saved = expenseRepository.save(expense);

        // A newly recorded expense above a small threshold enters the approval queue.
        approvalRepository.save(Approval.builder()
                .type("expense")
                .reference(saved.getCategory() + " - " + saved.getVendor())
                .amount(saved.getAmount())
                .submittedBy(saved.getSubmittedBy())
                .status("pending")
                .build());

        return mapper.toResponse(saved);
    }
}
