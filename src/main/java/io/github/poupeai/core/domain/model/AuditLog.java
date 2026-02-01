package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private Long id;
    private UUID profileId;
    private OffsetDateTime actionTime;
    private String actionType;
    private String entityType;
    private String entityId;
    private Map<String, Object> changes;
    private String sourceIp;
    private UUID correlationId;
    private String serviceName;
}
