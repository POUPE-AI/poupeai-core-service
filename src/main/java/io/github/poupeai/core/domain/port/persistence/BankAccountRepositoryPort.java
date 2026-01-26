package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.BankAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepositoryPort {
    BankAccount create(BankAccount bankAccount);
    BankAccount update(BankAccount bankAccount);
    Optional<BankAccount> findByIdAndProfileId(UUID id, UUID profileId);
    List<BankAccount> findAllByProfileId(UUID profileId);
    void delete(UUID id);
    boolean existsByIdAndProfileId(UUID id, UUID profileId);
    boolean isNameTaken(String name, UUID profileId, UUID excludeId);
    Optional<BankAccount> findDefaultByProfileId(UUID profileId);
    long countByProfileId(UUID profileId);
    void clearDefaultByProfileId(UUID profileId);
}
