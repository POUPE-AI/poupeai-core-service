package io.github.poupeai.core.domain.port.external;

import java.util.UUID;

public interface KeycloakUserPort {
    boolean deleteUser(UUID userId);
}
