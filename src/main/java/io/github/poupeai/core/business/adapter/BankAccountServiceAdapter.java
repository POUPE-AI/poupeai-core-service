package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ForbiddenActionException;
import io.github.poupeai.core.domain.exception.ResourceAlreadyExistsException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.BankAccountServicePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountServiceAdapter implements BankAccountServicePort {
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final InstitutionRepositoryPort institutionRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Override
    @Transactional
    public BankAccount create(BankAccount bankAccount) {
        validateBankAccount(bankAccount);

        long accountCount = bankAccountRepositoryPort.countByProfileId(bankAccount.getProfileId());
        if (accountCount == 0) {
            bankAccount.setIsDefault(true);
        }

        if (Boolean.TRUE.equals(bankAccount.getIsDefault())) {
            bankAccountRepositoryPort.clearDefaultByProfileId(bankAccount.getProfileId());
        }

        return bankAccountRepositoryPort.create(bankAccount);
    }

    @Override
    @Transactional
    public BankAccount update(BankAccount bankAccount, UUID profileId) {
        validateBankAccount(bankAccount);

        if (Boolean.TRUE.equals(bankAccount.getIsDefault())) {
            bankAccountRepositoryPort.clearDefaultByProfileId(profileId);
        }

        return bankAccountRepositoryPort.update(bankAccount);
    }

    @Override
    public BankAccount findByIdAndProfileId(UUID id, UUID profileId) {
        return bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada."));
    }

    @Override
    public List<BankAccount> findAllByProfileId(UUID profileId) {
        return bankAccountRepositoryPort.findAllByProfileId(profileId);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID profileId) {
        BankAccount bankAccount = bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada."));

        if (Boolean.TRUE.equals(bankAccount.getIsDefault())) {
            long accountCount = bankAccountRepositoryPort.countByProfileId(profileId);
            if (accountCount > 1) {
                throw new ForbiddenActionException("Não é possível excluir a conta bancária padrão. Defina outra conta como padrão antes de excluir esta.");
            }
        }

        bankAccountRepositoryPort.delete(id);
    }

    @Override
    public BigDecimal calculateCurrentBalance(UUID bankAccountId, UUID profileId) {
        BankAccount bankAccount = bankAccountRepositoryPort.findByIdAndProfileId(bankAccountId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada."));

        BigDecimal initialBalance = bankAccount.getInitialBalance() != null 
                ? bankAccount.getInitialBalance() 
                : BigDecimal.ZERO;

        BigDecimal incomeTotal = transactionRepositoryPort.sumAmountByBankAccountIdAndType(
                bankAccountId, TransactionType.INCOME);
        BigDecimal expenseTotal = transactionRepositoryPort.sumAmountByBankAccountIdAndType(
                bankAccountId, TransactionType.EXPENSE);

        return initialBalance.add(incomeTotal).subtract(expenseTotal);
    }

    private void validateBankAccount(BankAccount bankAccount) {
        if (bankAccount.getInstitution() != null && bankAccount.getInstitution().getId() != null) {
            if (!institutionRepositoryPort.existsById(bankAccount.getInstitution().getId())) {
                throw new ResourceNotFoundException("Instituição financeira não encontrada.");
            }
        }

        if (bankAccountRepositoryPort.isNameTaken(bankAccount.getName(), bankAccount.getProfileId(), bankAccount.getId())) {
            throw new ResourceAlreadyExistsException("Uma conta bancária com este nome já existe para este perfil.");
        }

        if (bankAccount.getInitialBalance() != null && bankAccount.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("O saldo inicial não pode ser negativo.");
        }
    }
}
