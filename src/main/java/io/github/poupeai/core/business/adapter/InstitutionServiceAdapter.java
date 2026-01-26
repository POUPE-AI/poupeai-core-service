package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.domain.port.business.InstitutionServicePort;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionServiceAdapter implements InstitutionServicePort {
    private final InstitutionRepositoryPort institutionRepositoryPort;

    @Override
    public List<Institution> findAll() {
        return institutionRepositoryPort.findAll();
    }
}
