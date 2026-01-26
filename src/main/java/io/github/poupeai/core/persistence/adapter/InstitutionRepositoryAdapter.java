package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;
import io.github.poupeai.core.persistence.mapper.InstitutionEntityMapper;
import io.github.poupeai.core.persistence.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InstitutionRepositoryAdapter implements InstitutionRepositoryPort {
    private final InstitutionRepository institutionRepository;
    private final InstitutionEntityMapper institutionMapper;

    @Override
    public List<Institution> findAll() {
        var entities = institutionRepository.findAll();
        return institutionMapper.toDomainList(entities);
    }

    @Override
    public boolean existsById(Long id) {
        return institutionRepository.existsById(id);
    }
}
