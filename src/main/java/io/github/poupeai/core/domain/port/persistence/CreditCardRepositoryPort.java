package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepositoryPort {
    CreditCard create(CreditCard creditCard);
    CreditCard update(CreditCard creditCard);
    Optional<CreditCard> findByIdAndProfileId(UUID id, UUID profileId);
    List<CreditCard> findAllByProfileId(UUID profileId);
    void delete(UUID id);
    boolean existsByIdAndProfileId(UUID id, UUID profileId);
    boolean isNameTaken(String name, UUID profileId, UUID excludeId);
}
