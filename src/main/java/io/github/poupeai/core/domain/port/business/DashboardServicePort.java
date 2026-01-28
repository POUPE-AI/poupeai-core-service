package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.DashboardData;

import java.time.YearMonth;
import java.util.UUID;

public interface DashboardServicePort {
    DashboardData getDashboardData(UUID profileId, YearMonth period);
}
