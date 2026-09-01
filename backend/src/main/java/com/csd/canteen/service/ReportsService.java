package com.csd.canteen.service;

import com.csd.canteen.dto.response.ReportsOverviewResponse;
import com.csd.canteen.dto.response.SalesByMonth;
import com.csd.canteen.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportsService {

    // Placeholder period totals until real sales imports feed this view (see BUILD-AND-DEPLOY.md).
    private static final BigDecimal PLACEHOLDER_SALES = new BigDecimal("8214000");
    private static final BigDecimal PLACEHOLDER_EXPENSES_FALLBACK = new BigDecimal("1468200");
    private static final BigDecimal PLACEHOLDER_PROFIT = new BigDecimal("6745800");

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public ReportsOverviewResponse overview() {
        BigDecimal totalExpenses = expenseRepository.findAll().stream()
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            totalExpenses = PLACEHOLDER_EXPENSES_FALLBACK;
        }

        List<SalesByMonth> byMonth = List.of(
                new SalesByMonth("Jun", new BigDecimal("2860000"), new BigDecimal("510000")),
                new SalesByMonth("Jul", new BigDecimal("3124000"), new BigDecimal("552000")),
                new SalesByMonth("Aug", new BigDecimal("2230000"), new BigDecimal("406200"))
        );

        return new ReportsOverviewResponse(PLACEHOLDER_SALES, totalExpenses, PLACEHOLDER_PROFIT, byMonth);
    }
}
