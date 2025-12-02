package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Profile;

import java.util.UUID;

public interface GetProfilePort {
    Profile execute(UUID userId);
}
