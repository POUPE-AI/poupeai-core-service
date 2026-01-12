package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.CreditCard;

import java.util.List;
import java.util.UUID;

public interface CreditCardServicePort {
    CreditCard create(CreditCard creditCard);
    CreditCard update(CreditCard creditCard, UUID profileId);
    CreditCard findByIdAndProfileId(UUID id, UUID profileId);
    List<CreditCard> findAllByProfileId(UUID profileId);
    void delete(UUID id, UUID profileId);
}
