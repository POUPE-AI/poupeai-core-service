package io.github.poupeai.core.web.controller.dashboard;

import io.github.poupeai.core.domain.model.DashboardData;
import io.github.poupeai.core.domain.port.business.DashboardServicePort;
import io.github.poupeai.core.web.dto.dashboard.DashboardResponse;
import io.github.poupeai.core.web.mapper.dashboard.DashboardControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dados do Dashboard")
public class DashboardController {
    private final DashboardServicePort dashboardService;
    private final DashboardControllerMapper mapper;

    @GetMapping
    @Operation(summary = "Obter dados do dashboard", description = "Retorna dados do dashboard para o período especificado. "
            , security = @SecurityRequirement(name = "bearer-key"))
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(hidden = true) @CurrentUserId String userIdStr,
            @Parameter(description = "Período no formato 'yyyy-MM'", example = "2026-01") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {

        UUID profileId = UUID.fromString(userIdStr);
        DashboardData data = dashboardService.getDashboardData(profileId, period);

        return ResponseEntity.ok(mapper.toResponse(data));
    }
}
