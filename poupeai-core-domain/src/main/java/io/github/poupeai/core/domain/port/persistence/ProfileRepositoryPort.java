package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Profile;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepositoryPort {
    Profile save(Profile profile);
    Optional<Profile> findById(UUID id);
    Optional<Profile> findByEmail(String email);
}
