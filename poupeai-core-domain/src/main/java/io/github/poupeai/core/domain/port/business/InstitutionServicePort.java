package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Institution;

import java.util.List;

public interface InstitutionServicePort {
    List<Institution> findAll();
}
