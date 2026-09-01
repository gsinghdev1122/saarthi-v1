package com.csd.canteen.controller;

import com.csd.canteen.dto.request.CreateExpenseRequest;
import com.csd.canteen.dto.response.ExpenseResponse;
import com.csd.canteen.service.ExpenseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Finance")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public List<ExpenseResponse> list() {
        return expenseService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CANTEEN_MANAGER','FINANCE_REVIEWER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest request) {
        return expenseService.create(request);
    }
}
