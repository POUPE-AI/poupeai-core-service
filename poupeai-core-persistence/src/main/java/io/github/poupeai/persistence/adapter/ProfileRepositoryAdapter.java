package io.github.poupeai.persistence.adapter;

import io.github.poupeai.domain.model.Profile;
import io.github.poupeai.domain.port.persistence.ProfileRepositoryPort;
import io.github.poupeai.persistence.mapper.ProfileEntityMapper;
import io.github.poupeai.persistence.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileRepositoryAdapter implements ProfileRepositoryPort {
    private final ProfileRepository repository;
    private final ProfileEntityMapper mapper;

    @Override
    public Profile save(Profile profile) {
        var entity = mapper.toEntity(profile);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Profile> findById(UUID userId) {
        return repository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Profile> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }
}
