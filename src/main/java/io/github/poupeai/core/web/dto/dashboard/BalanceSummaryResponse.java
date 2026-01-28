package io.github.poupeai.core.web.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSummaryResponse {
    private BigDecimal currentTotal;

    private BigDecimal difference;

    private List<BalanceChartPointResponse> chartData;
}
