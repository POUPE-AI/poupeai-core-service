package io.github.poupeai.domain.port.business;

import io.github.poupeai.domain.model.Profile;

public interface CreateOrUpdateProfilePort {
    Profile execute(Profile profile);
}
