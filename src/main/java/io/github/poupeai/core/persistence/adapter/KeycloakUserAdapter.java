package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.port.external.KeycloakUserPort;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserAdapter implements KeycloakUserPort {
    private final Keycloak keycloakAdminClient;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Override
    public boolean deleteUser(UUID userId) {
        String userIdStr = userId.toString();
        log.info("Tentando deletar usuário no Keycloak: {}", userIdStr);

        try {
            UsersResource usersResource = keycloakAdminClient.realm(realm).users();
            usersResource.delete(userIdStr);
            log.info("Usuário {} deletado com sucesso do Keycloak", userIdStr);
            return true;
        } catch (NotFoundException e) {
            log.warn("Usuário {} não encontrado no Keycloak, considerando como já deletado", userIdStr);
            return true;
        } catch (Exception e) {
            log.error("Erro ao deletar usuário {} do Keycloak: {}", userIdStr, e.getMessage(), e);
            return false;
        }
    }
}
