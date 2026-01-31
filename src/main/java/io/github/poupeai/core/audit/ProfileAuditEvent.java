package io.github.poupeai.core.audit;

import java.util.Map;
import java.util.UUID;

public record ProfileAuditEvent(
        UUID profileId,
        String actionType,
        String entityType,
        Map<String, Object> changes,
        String sourceIp,
        UUID correlationId) {
}
