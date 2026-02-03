package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.audit.Log;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.CreateOrUpdateProfilePort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrUpdateProfileAdapter implements CreateOrUpdateProfilePort {
    private final ProfileRepositoryPort profileRepositoryPort;

    @Override
    @Transactional
    public Profile execute(final Profile profile) {
        final Profile[] result = new Profile[1];

        Map<String, String> context = Map.of(
                "user.id", profile.getUserId().toString(),
                "event.type", "PROFILE_SYNC"
        );

        Log.run(context, () -> {
            result[0] = profileRepositoryPort.findById(profile.getUserId())
                    .map(existing -> {
                        log.info("Perfil atualizado via sincronização.");
                        return updateExisting(existing, profile);
                    })
                    .orElseGet(() -> {
                        log.info("Novo perfil criado via sincronização.");
                        return createNewProfile(profile);
                    });
        });

        return result[0];
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
