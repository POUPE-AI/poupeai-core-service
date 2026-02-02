package io.github.poupeai.core.audit;

import io.github.poupeai.core.persistence.entity.AuditLogEntity;
import io.github.poupeai.core.persistence.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileAuditEventHandler {

    private static final String SERVICE_NAME = "poupeai-core-service";

    private final AuditLogRepository auditLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProfileAuditEvent(ProfileAuditEvent event) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .profileId(event.profileId())
                    .actionTime(OffsetDateTime.now())
                    .actionType(event.actionType())
                    .entityType(event.entityType())
                    .entityId(event.profileId() != null ? event.profileId().toString() : "unknown")
                    .changes(event.changes())
                    .sourceIp(event.sourceIp())
                    .correlationId(event.correlationId())
                    .serviceName(SERVICE_NAME)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Falha ao criar log de auditoria para Profile {}: {}", event.actionType(), e.getMessage(), e);
        }
    }
}
