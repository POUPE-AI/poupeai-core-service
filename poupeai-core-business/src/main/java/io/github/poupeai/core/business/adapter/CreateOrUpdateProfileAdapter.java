package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.CreateOrUpdateProfilePort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrUpdateProfileAdapter implements CreateOrUpdateProfilePort {
    private final ProfileRepositoryPort profileRepositoryPort;

    @Override
    @Transactional
    public Profile execute(final Profile profile) {
        return profileRepositoryPort.findById(profile.getUserId())
                .map(existing -> updateExisting(existing, profile))
                .orElseGet(() -> createNewProfile(profile));
    }

    private Profile updateExisting(final Profile profileExisting, final Profile newData) {
        profileExisting.setFirstName(newData.getFirstName());
        profileExisting.setLastName(newData.getLastName());
        profileExisting.setEmail(newData.getEmail());
        return profileRepositoryPort.save(profileExisting);
    }

    private Profile createNewProfile(final Profile profile) {
        return profileRepositoryPort.save(profile);
    }
}
