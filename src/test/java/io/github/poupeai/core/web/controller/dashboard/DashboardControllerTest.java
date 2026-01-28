package io.github.poupeai.core.web.controller.dashboard;

import io.github.poupeai.core.domain.model.BalanceSummary;
import io.github.poupeai.core.domain.model.CategorySummary;
import io.github.poupeai.core.domain.model.DashboardData;
import io.github.poupeai.core.domain.model.InvoicesSummary;
import io.github.poupeai.core.domain.port.business.DashboardServicePort;
import io.github.poupeai.core.web.dto.dashboard.BalanceSummaryResponse;
import io.github.poupeai.core.web.dto.dashboard.CategorySummaryResponse;
import io.github.poupeai.core.web.dto.dashboard.DashboardResponse;
import io.github.poupeai.core.web.dto.dashboard.InvoicesSummaryResponse;
import io.github.poupeai.core.web.mapper.dashboard.DashboardControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardServicePort dashboardService;

    @Mock
    private DashboardControllerMapper mapper;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("Should get dashboard data with period parameter")
    void shouldGetDashboardDataWithPeriod() {
        UUID userId = UUID.randomUUID();
        YearMonth period = YearMonth.of(2026, 1);

        DashboardData dashboardData = DashboardData.builder()
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .balance(BalanceSummary.builder()
                        .currentTotal(new BigDecimal("5000.00"))
                        .difference(new BigDecimal("15.50"))
                        .chartData(List.of())
                        .build())
                .incomes(CategorySummary.builder()
                        .currentTotal(new BigDecimal("3000.00"))
                        .difference(new BigDecimal("10.00"))
                        .chartData(List.of())
                        .build())
                .expenses(CategorySummary.builder()
                        .currentTotal(new BigDecimal("2000.00"))
                        .difference(new BigDecimal("-5.00"))
                        .chartData(List.of())
                        .build())
                .invoices(InvoicesSummary.builder()
                        .currentTotal(new BigDecimal("1500.00"))
                        .difference(new BigDecimal("0.00"))
                        .chartData(List.of())
                        .build())
                .build();

        DashboardResponse expectedResponse = DashboardResponse.builder()
                .message("Dashboard data retrieved successfully.")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .balance(BalanceSummaryResponse.builder()
                        .currentTotal(new BigDecimal("5000.00"))
                        .difference(new BigDecimal("15.50"))
                        .chartData(List.of())
                        .build())
                .incomes(CategorySummaryResponse.builder()
                        .currentTotal(new BigDecimal("3000.00"))
                        .difference(new BigDecimal("10.00"))
                        .chartData(List.of())
                        .build())
                .expenses(CategorySummaryResponse.builder()
                        .currentTotal(new BigDecimal("2000.00"))
                        .difference(new BigDecimal("-5.00"))
                        .chartData(List.of())
                        .build())
                .invoices(InvoicesSummaryResponse.builder()
                        .currentTotal(new BigDecimal("1500.00"))
                        .difference(new BigDecimal("0.00"))
                        .chartData(List.of())
                        .build())
                .build();

        when(dashboardService.getDashboardData(userId, period)).thenReturn(dashboardData);
        when(mapper.toResponse(dashboardData)).thenReturn(expectedResponse);

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(userId.toString(), period);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Dashboard data retrieved successfully.", result.getBody().getMessage());
        assertEquals(new BigDecimal("5000.00"), result.getBody().getBalance().getCurrentTotal());
        verify(dashboardService).getDashboardData(userId, period);
    }

    @Test
    @DisplayName("Should get dashboard data without period parameter")
    void shouldGetDashboardDataWithoutPeriod() {
        UUID userId = UUID.randomUUID();

        DashboardData dashboardData = DashboardData.builder()
                .startDate(LocalDate.now().minusDays(29))
                .endDate(LocalDate.now())
                .balance(BalanceSummary.builder()
                        .currentTotal(BigDecimal.ZERO)
                        .difference(BigDecimal.ZERO)
                        .chartData(List.of())
                        .build())
                .incomes(CategorySummary.builder()
                        .currentTotal(BigDecimal.ZERO)
                        .difference(BigDecimal.ZERO)
                        .chartData(List.of())
                        .build())
                .expenses(CategorySummary.builder()
                        .currentTotal(BigDecimal.ZERO)
                        .difference(BigDecimal.ZERO)
                        .chartData(List.of())
                        .build())
                .invoices(InvoicesSummary.builder()
                        .currentTotal(BigDecimal.ZERO)
                        .difference(BigDecimal.ZERO)
                        .chartData(List.of())
                        .build())
                .build();

        DashboardResponse expectedResponse = DashboardResponse.builder()
                .message("Dashboard data retrieved successfully.")
                .startDate(LocalDate.now().minusDays(29))
                .endDate(LocalDate.now())
                .build();

        when(dashboardService.getDashboardData(eq(userId), any())).thenReturn(dashboardData);
        when(mapper.toResponse(dashboardData)).thenReturn(expectedResponse);

        ResponseEntity<DashboardResponse> result = dashboardController.getDashboard(userId.toString(), null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(dashboardService).getDashboardData(userId, null);
    }
}
