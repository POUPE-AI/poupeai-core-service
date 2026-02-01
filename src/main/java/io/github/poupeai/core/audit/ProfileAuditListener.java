package io.github.poupeai.core.audit;

import io.github.poupeai.core.persistence.entity.ProfileEntity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

@Slf4j
public class ProfileAuditListener {

    private static final String ENTITY_TYPE = "Profile";

    private static final Map<UUID, Map<String, Object>> OLD_VALUES_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    @PostLoad
    public void postLoad(ProfileEntity entity) {
        try {
            if (entity.getUserId() != null) {
                OLD_VALUES_CACHE.put(entity.getUserId(), captureEntityState(entity));
            }
        } catch (Exception e) {
            log.debug("Falha ao capturar estado da entidade no load: {}", e.getMessage());
        }
    }

    @PostPersist
    public void postPersist(ProfileEntity entity) {
        try {
            publishAuditEvent(entity, "CREATE", null);
            log.info("Evento de auditoria publicado para ação: CREATE, profile_id: {}", entity.getUserId());
        } catch (Exception e) {
            log.error("Falha ao publicar evento de auditoria para Profile CREATE: {}", e.getMessage(), e);
            // Clean up any cached state for this entity to avoid stale data on failed CREATE operations
            if (entity != null && entity.getUserId() != null) {
                OLD_VALUES_CACHE.remove(entity.getUserId());
            }
        }
    }

    @PostUpdate
    public void postUpdate(ProfileEntity entity) {
        try {
            OLD_VALUES_CACHE.compute(entity.getUserId(), (id, oldValues) -> {
                Map<String, Object> newState = captureEntityState(entity);
                Map<String, Object> changes = detectChanges(oldValues, newState);

                if (changes.isEmpty()) {
                    log.debug("Sem mudanças detectadas no perfil: {}", entity.getUserId());
                    return newState;
                }

                publishAuditEvent(entity, "UPDATE", changes);
                log.info("Evento de auditoria publicado para ação: UPDATE, profile_id: {}, changes: {}",
                        entity.getUserId(), changes.keySet());

                return newState;
            });
        } catch (Exception e) {
            log.error("Falha ao publicar evento de auditoria para Profile UPDATE: {}", e.getMessage(), e);
        }
    }

    @PostRemove
    public void postRemove(ProfileEntity entity) {
        try {
            publishAuditEvent(entity, "DELETE", null);
            log.info("Evento de auditoria publicado para ação: DELETE, profile_id: {}", entity.getUserId());

            OLD_VALUES_CACHE.remove(entity.getUserId());
        } catch (Exception e) {
            log.error("Falha ao publicar evento de auditoria para Profile DELETE: {}", e.getMessage(), e);
        }
    }

    private void publishAuditEvent(ProfileEntity entity, String actionType, Map<String, Object> changes) {
        var context = ApplicationContextProvider.getApplicationContext();
        if (context == null) {
            log.warn("ApplicationContext não disponível, pulando publicação de evento de auditoria");
            return;
        }

        ProfileAuditEvent event = new ProfileAuditEvent(
                entity.getUserId(),
                actionType,
                ENTITY_TYPE,
                changes,
                AuditContext.getSourceIp(),
                AuditContext.getCorrelationId());

        context.publishEvent(event);
    }

    private Map<String, Object> captureEntityState(ProfileEntity entity) {
        Map<String, Object> state = new HashMap<>();
        state.put("email", entity.getEmail());
        state.put("firstName", entity.getFirstName());
        state.put("lastName", entity.getLastName());
        state.put("isDeactivated", entity.isDeactivated());
        state.put("deactivationScheduledAt",
                entity.getDeactivationScheduledAt() != null
                        ? entity.getDeactivationScheduledAt().toString()
                        : null);
        return state;
    }

    private Map<String, Object> detectChanges(Map<String, Object> oldValues, Map<String, Object> newValues) {
        Map<String, Object> changes = new HashMap<>();

        if (oldValues == null) {
            return changes;
        }

        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            String field = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldValues.get(field);

            if (!Objects.equals(oldValue, newValue)) {
                changes.put(field, new Object[] {
                        oldValue != null ? oldValue.toString() : null,
                        newValue != null ? newValue.toString() : null
                });
            }
        }

        return changes;
    }
}
