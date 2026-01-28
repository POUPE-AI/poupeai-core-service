package io.github.poupeai.core.web.mapper.dashboard;

import io.github.poupeai.core.domain.model.BalanceChartPoint;
import io.github.poupeai.core.domain.model.BalanceSummary;
import io.github.poupeai.core.domain.model.CategoryChartPoint;
import io.github.poupeai.core.domain.model.CategorySummary;
import io.github.poupeai.core.domain.model.DashboardData;
import io.github.poupeai.core.domain.model.InvoiceChartData;
import io.github.poupeai.core.domain.model.InvoicesSummary;
import io.github.poupeai.core.domain.model.SavingsEstimate;
import io.github.poupeai.core.web.dto.dashboard.BalanceChartPointResponse;
import io.github.poupeai.core.web.dto.dashboard.BalanceSummaryResponse;
import io.github.poupeai.core.web.dto.dashboard.CategoryChartPointResponse;
import io.github.poupeai.core.web.dto.dashboard.CategorySummaryResponse;
import io.github.poupeai.core.web.dto.dashboard.DashboardResponse;
import io.github.poupeai.core.web.dto.dashboard.InvoiceChartDataResponse;
import io.github.poupeai.core.web.dto.dashboard.InvoicesSummaryResponse;
import io.github.poupeai.core.web.dto.dashboard.SavingsEstimateResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DashboardControllerMapper {

    default DashboardResponse toResponse(DashboardData data) {
        return DashboardResponse.builder()
                .message("Dados do dashboard recuperados com sucesso.")
                .startDate(data.getStartDate())
                .endDate(data.getEndDate())
                .balance(toBalanceSummaryResponse(data.getBalance()))
                .incomes(toCategorySummaryResponse(data.getIncomes()))
                .expenses(toCategorySummaryResponse(data.getExpenses()))
                .invoices(toInvoicesSummaryResponse(data.getInvoices()))
                .estimatedSaving(toSavingsEstimateResponse(data.getEstimatedSaving()))
                .build();
    }

    BalanceSummaryResponse toBalanceSummaryResponse(BalanceSummary balance);

    BalanceChartPointResponse toBalanceChartPointResponse(BalanceChartPoint point);

    List<BalanceChartPointResponse> toBalanceChartPointResponseList(List<BalanceChartPoint> points);

    CategorySummaryResponse toCategorySummaryResponse(CategorySummary category);

    CategoryChartPointResponse toCategoryChartPointResponse(CategoryChartPoint point);

    List<CategoryChartPointResponse> toCategoryChartPointResponseList(List<CategoryChartPoint> points);

    InvoicesSummaryResponse toInvoicesSummaryResponse(InvoicesSummary invoices);

    InvoiceChartDataResponse toInvoiceChartDataResponse(InvoiceChartData data);

    List<InvoiceChartDataResponse> toInvoiceChartDataResponseList(List<InvoiceChartData> data);

    SavingsEstimateResponse toSavingsEstimateResponse(SavingsEstimate estimate);
}
