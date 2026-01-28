package io.github.poupeai.core.web.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceChartDataResponse {
    private String creditCardName;

    private String month;

    private BigDecimal totalAmount;

    private Boolean paid;

    private LocalDate dueDate;
}
