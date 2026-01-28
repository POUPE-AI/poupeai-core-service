package io.github.poupeai.core.web.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String message;

    private LocalDate startDate;

    private LocalDate endDate;

    private BalanceSummaryResponse balance;

    private CategorySummaryResponse incomes;

    private CategorySummaryResponse expenses;

    private InvoicesSummaryResponse invoices;

    private SavingsEstimateResponse estimatedSaving;
}
