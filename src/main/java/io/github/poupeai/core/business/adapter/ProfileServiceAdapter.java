package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.model.ProfileNotificationData;
import io.github.poupeai.core.domain.port.business.ProfileServicePort;
import io.github.poupeai.core.domain.port.external.KeycloakUserPort;
import io.github.poupeai.core.domain.port.messaging.ProfileNotificationProducerPort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceAdapter implements ProfileServicePort {
    private static final int DELETION_GRACE_PERIOD_DAYS = 30;
    private static final String REACTIVATE_ACCOUNT_DEEP_LINK = "poupeai://app/reactivate-account";

    private final ProfileRepositoryPort profileRepositoryPort;
    private final ProfileNotificationProducerPort profileNotificationProducerPort;
    private final KeycloakUserPort keycloakUserPort;

    @Override
    @Transactional
    public void deactivate(UUID userId) {
        log.info("Iniciando desativação do perfil para userId: {}", userId);

        Profile profile = profileRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));

        if (profile.isDeactivated()) {
            log.warn("Tentativa de desativar perfil já desativado: {}", userId);
            throw new DomainException("Perfil já está desativado ou possui exclusão agendada.");
        }

        OffsetDateTime deletionScheduledAt = OffsetDateTime.now().plusDays(DELETION_GRACE_PERIOD_DAYS);
        profile.setDeactivated(true);
        profile.setDeactivationScheduledAt(deletionScheduledAt);

        profileRepositoryPort.save(profile);
        log.info("Perfil desativado com sucesso. Exclusão agendada para: {}", deletionScheduledAt);

        publishDeletionScheduledEvent(profile);
    }

    @Override
    @Transactional
    public void reactivate(UUID userId) {
        log.info("Iniciando reativação do perfil para userId: {}", userId);

        Profile profile = profileRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));

        if (!profile.isDeactivated()) {
            log.warn("Tentativa de reativar perfil já ativo: {}", userId);
            throw new DomainException("Perfil já está ativo.");
        }

        profile.setDeactivated(false);
        profile.setDeactivationScheduledAt(null);

        profileRepositoryPort.save(profile);
        log.info("Perfil reativado com sucesso: {}", userId);
    }

    @Override
    @Transactional
    public void hardDeleteExpiredProfiles() {
        log.info("Iniciando task de exclusão permanente de perfis expirados");

        OffsetDateTime currentTime = OffsetDateTime.now();
        var expiredProfiles = profileRepositoryPort.findExpiredDeactivatedProfiles(currentTime);

        if (expiredProfiles.isEmpty()) {
            log.info("Nenhum perfil expirado para remover");
            return;
        }

        log.info("Encontrados {} perfis expirados para exclusão permanente", expiredProfiles.size());

        int deletedCount = 0;
        List<UUID> failedIds = new ArrayList<>();

        for (Profile profile : expiredProfiles) {
            UUID userId = profile.getUserId();
            log.info("Processando exclusão do perfil: {} ({})", userId, profile.getEmail());

            boolean keycloakDeletionSuccessful = keycloakUserPort.deleteUser(userId);

            if (keycloakDeletionSuccessful) {
                try {
                    profileRepositoryPort.delete(userId);
                    deletedCount++;
                    log.info("Perfil {} excluído permanentemente com sucesso", userId);
                } catch (Exception e) {
                    log.error("Falha ao excluir perfil local {}: {}", userId, e.getMessage(), e);
                    failedIds.add(userId);
                }
            } else {
                log.error("Falha ao excluir usuário {} do Keycloak", userId);
                failedIds.add(userId);
            }
        }

        String summary = String.format("Removidos %d perfis expirados.", deletedCount);
        if (!failedIds.isEmpty()) {
            summary += String.format(" Falha ao remover perfis: %s.", failedIds);
        }

        log.info("Task de exclusão finalizado. {}", summary);
    }

    private void publishDeletionScheduledEvent(Profile profile) {
        ProfileNotificationData payload = ProfileNotificationData.builder()
                .deletionScheduledAt(profile.getDeactivationScheduledAt())
                .reactivateAccountDeepLink(REACTIVATE_ACCOUNT_DEEP_LINK)
                .build();

        PoupeAiEvent<ProfileNotificationData> event = PoupeAiEvent.<ProfileNotificationData>builder()
                .messageId(UUID.randomUUID())
                .timestamp(OffsetDateTime.now())
                .triggerType("USER_ACTION")
                .eventType("PROFILE_DELETION_SCHEDULED")
                .recipient(PoupeAiEvent.Recipient.builder()
                        .userId(profile.getUserId().toString())
                        .email(profile.getEmail())
                        .name(buildUserName(profile))
                        .build())
                .payload(payload)
                .build();

        profileNotificationProducerPort.publishDeletionScheduled(event);
        log.info("Evento PROFILE_DELETION_SCHEDULED publicado para userId: {}", profile.getUserId());
    }

    private String buildUserName(Profile profile) {
        StringBuilder name = new StringBuilder();
        if (profile.getFirstName() != null) {
            name.append(profile.getFirstName());
        }
        if (profile.getLastName() != null) {
            if (!name.isEmpty()) {
                name.append(" ");
            }
            name.append(profile.getLastName());
        }
        return name.toString().trim();
    }
}
