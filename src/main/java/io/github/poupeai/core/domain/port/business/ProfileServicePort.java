package io.github.poupeai.core.domain.port.business;

import java.util.UUID;

public interface ProfileServicePort {
    void deactivate(UUID userId);

    void reactivate(UUID userId);

    void hardDeleteExpiredProfiles();
}
