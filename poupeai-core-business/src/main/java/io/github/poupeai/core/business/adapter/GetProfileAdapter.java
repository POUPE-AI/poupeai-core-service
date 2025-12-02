package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.GetProfilePort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProfileAdapter implements GetProfilePort {
    private final ProfileRepositoryPort profileRepositoryPort;

    public Profile execute(UUID profileId) {
        return profileRepositoryPort.findById(profileId)
                .orElseThrow(() -> new DomainException("Profile not found for user: " + profileId));
    }
}
