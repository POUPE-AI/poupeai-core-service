package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Profile;

public interface CreateOrUpdateProfilePort {
    Profile execute(Profile profile);
}
