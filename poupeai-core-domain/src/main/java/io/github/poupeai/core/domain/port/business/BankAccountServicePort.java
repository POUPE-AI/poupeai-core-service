package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.BankAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BankAccountServicePort {
    BankAccount create(BankAccount bankAccount);
    BankAccount update(BankAccount bankAccount, UUID profileId);
    BankAccount findByIdAndProfileId(UUID id, UUID profileId);
    List<BankAccount> findAllByProfileId(UUID profileId);
    void delete(UUID id, UUID profileId);
    BigDecimal calculateCurrentBalance(UUID bankAccountId, UUID profileId);
}
