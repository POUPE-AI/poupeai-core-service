package io.github.poupeai.core.application.scheduler;

import io.github.poupeai.core.domain.port.business.ProfileServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileDeletionScheduler {
    private final ProfileServicePort profileServicePort;

    @Scheduled(cron = "${app.scheduler.profile.deletion-cron}")
    public void processExpiredProfiles() {
        log.info("Iniciando processamento de exclusão permanente de perfis expirados");
        try {
            profileServicePort.hardDeleteExpiredProfiles();
        } catch (Exception e) {
            log.error("Erro durante execução do processamento de exclusão de perfis: {}", e.getMessage(), e);
        }
        log.info("Processamento de exclusão permanente de perfis finalizado");
    }
}
