package io.github.poupeai.core.domain.model;

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
public class CategorySummary {
    private BigDecimal currentTotal;
    private BigDecimal difference;
    private List<CategoryChartPoint> chartData;
}
