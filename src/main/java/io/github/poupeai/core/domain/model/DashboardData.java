package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    private LocalDate startDate;
    private LocalDate endDate;
    private BalanceSummary balance;
    private CategorySummary incomes;
    private CategorySummary expenses;
    private InvoicesSummary invoices;
    private SavingsEstimate estimatedSaving;
}
