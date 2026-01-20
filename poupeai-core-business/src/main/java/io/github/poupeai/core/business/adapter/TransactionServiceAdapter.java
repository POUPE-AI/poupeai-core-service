package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceAdapter implements TransactionServicePort {
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CreditCardRepositoryPort creditCardRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final InvoiceServicePort invoiceServicePort;

    @Override
    @Transactional
    public Transaction create(Transaction transaction) {
        validateTransaction(transaction);

        Category category = categoryRepositoryPort.findByIdAndProfileId(
                transaction.getCategoryId(), transaction.getProfileId()
        ).orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        
        TransactionType derivedType = mapCategoryTypeToTransactionType(category.getType());
        transaction.setType(derivedType);

        if (transaction.getCreditCardId() != null) {
            if (derivedType != TransactionType.EXPENSE) {
                throw new DomainException("Transações de cartão de crédito devem ser do tipo despesa.");
            }
            return createCreditCardTransaction(transaction);
        }

        return transactionRepositoryPort.create(transaction);
    }
    
    private TransactionType mapCategoryTypeToTransactionType(CategoryType categoryType) {
        return switch (categoryType) {
            case INCOME -> TransactionType.INCOME;
            case EXPENSE -> TransactionType.EXPENSE;
        };
    }

    @Override
    @Transactional
    public Transaction update(Transaction transaction, UUID profileId) {
        Transaction existingTransaction = transactionRepositoryPort.findByIdAndProfileId(transaction.getId(), profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));

        if (Boolean.TRUE.equals(existingTransaction.getIsInstallment())) {
            throw new DomainException("Não é possível editar uma transação parcelada. Delete todas as parcelas e crie novamente.");
        }

        validateTransactionForUpdate(transaction, existingTransaction);

        UUID categoryId = transaction.getCategoryId() != null ? transaction.getCategoryId() : existingTransaction.getCategoryId();
        Category category = categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        TransactionType derivedType = mapCategoryTypeToTransactionType(category.getType());
        transaction.setType(derivedType);

        UUID creditCardId = existingTransaction.getCreditCardId();
        if (creditCardId != null && derivedType != TransactionType.EXPENSE) {
            throw new DomainException("Transações de cartão de crédito devem ser do tipo despesa.");
        }

        UUID oldInvoiceId = existingTransaction.getInvoiceId();

        Transaction updatedTransaction = transactionRepositoryPort.update(transaction);

        if (oldInvoiceId != null) {
            invoiceServicePort.updateInvoiceTotals(oldInvoiceId);
        }
        if (transaction.getInvoiceId() != null && !transaction.getInvoiceId().equals(oldInvoiceId)) {
            invoiceServicePort.updateInvoiceTotals(transaction.getInvoiceId());
        }

        return updatedTransaction;
    }

    @Override
    public Transaction findByIdAndProfileId(UUID id, UUID profileId) {
        return transactionRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));
    }

    @Override
    public List<Transaction> findAllByProfileId(UUID profileId) {
        return transactionRepositoryPort.findAllByProfileId(profileId);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID profileId) {
        Transaction transaction = transactionRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));

        UUID invoiceId = transaction.getInvoiceId();

        if (Boolean.TRUE.equals(transaction.getIsInstallment()) && transaction.getPurchaseGroupUuid() != null) {
            transactionRepositoryPort.deleteByPurchaseGroupUuid(transaction.getPurchaseGroupUuid());
        } else {
            transactionRepositoryPort.delete(id);
        }

        if (invoiceId != null) {
            invoiceServicePort.updateInvoiceTotals(invoiceId);
        }
    }

    private Transaction createCreditCardTransaction(Transaction transaction) {
        CreditCard creditCard = creditCardRepositoryPort.findByIdAndProfileId(
                transaction.getCreditCardId(), transaction.getProfileId()
        ).orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));

        if (Boolean.TRUE.equals(transaction.getIsInstallment()) && 
            transaction.getTotalInstallments() != null && 
            transaction.getTotalInstallments() > 1) {
            return createInstallmentTransactions(transaction, creditCard);
        }

        Invoice invoice = invoiceServicePort.getOrCreateInvoiceForDate(creditCard, transaction.getTransactionDate());
        transaction.setInvoiceId(invoice.getId());

        Transaction savedTransaction = transactionRepositoryPort.create(transaction);
        
        invoiceServicePort.updateInvoiceTotals(invoice.getId());

        return savedTransaction;
    }

    private Transaction createInstallmentTransactions(Transaction transaction, CreditCard creditCard) {
        int totalInstallments = transaction.getTotalInstallments();
        BigDecimal totalAmount = transaction.getAmount();
        BigDecimal installmentAmount = totalAmount.divide(
                BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP
        );
        
        BigDecimal remainder = totalAmount.subtract(
                installmentAmount.multiply(BigDecimal.valueOf(totalInstallments))
        );

        UUID purchaseGroupUuid = UUID.randomUUID();
        LocalDate currentDate = transaction.getTransactionDate();
        List<Transaction> installments = new ArrayList<>();
        List<UUID> invoiceIdsToUpdate = new ArrayList<>();

        for (int i = 1; i <= totalInstallments; i++) {
            Invoice invoice = invoiceServicePort.getOrCreateInvoiceForDate(creditCard, currentDate);
            
            BigDecimal currentInstallmentAmount = installmentAmount;
            if (i == totalInstallments && remainder.compareTo(BigDecimal.ZERO) != 0) {
                currentInstallmentAmount = installmentAmount.add(remainder);
            }

            Transaction installment = Transaction.builder()
                    .profileId(transaction.getProfileId())
                    .description(String.format("%s (%d/%d)", transaction.getDescription(), i, totalInstallments))
                    .amount(currentInstallmentAmount)
                    .type(transaction.getType())
                    .transactionDate(currentDate)
                    .bankAccountId(null)
                    .creditCardId(transaction.getCreditCardId())
                    .categoryId(transaction.getCategoryId())
                    .invoiceId(invoice.getId())
                    .attachmentKey(transaction.getAttachmentKey())
                    .isInstallment(true)
                    .installmentNumber(i)
                    .totalInstallments(totalInstallments)
                    .purchaseGroupUuid(purchaseGroupUuid)
                    .originalStatementId(transaction.getOriginalStatementId())
                    .originalStatementDescription(transaction.getOriginalStatementDescription())
                    .build();

            installments.add(installment);
            
            if (!invoiceIdsToUpdate.contains(invoice.getId())) {
                invoiceIdsToUpdate.add(invoice.getId());
            }

            currentDate = currentDate.plusMonths(1);
        }

        List<Transaction> savedInstallments = transactionRepositoryPort.createAll(installments);

        for (UUID invoiceId : invoiceIdsToUpdate) {
            invoiceServicePort.updateInvoiceTotals(invoiceId);
        }

        return savedInstallments.get(0);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor da transação deve ser maior que zero.");
        }

        if (transaction.getDescription() == null || transaction.getDescription().trim().isEmpty()) {
            throw new DomainException("A descrição da transação é obrigatória.");
        }

        if (transaction.getTransactionDate() == null) {
            throw new DomainException("A data da transação é obrigatória.");
        }

        if (transaction.getCategoryId() == null) {
            throw new DomainException("A categoria é obrigatória.");
        }

        if (transaction.getBankAccountId() == null && transaction.getCreditCardId() == null) {
            throw new DomainException("A transação deve estar associada a uma conta bancária ou cartão de crédito.");
        }

        if (transaction.getBankAccountId() != null && transaction.getCreditCardId() != null) {
            throw new DomainException("A transação não pode estar associada a uma conta bancária e cartão de crédito simultaneamente.");
        }

        if (transaction.getBankAccountId() != null) {
            if (!bankAccountRepositoryPort.existsByIdAndProfileId(transaction.getBankAccountId(), transaction.getProfileId())) {
                throw new ResourceNotFoundException("Conta bancária não encontrada.");
            }
        }

        if (Boolean.TRUE.equals(transaction.getIsInstallment())) {
            if (transaction.getBankAccountId() != null) {
                throw new DomainException("Parcelamento só é permitido para transações de cartão de crédito.");
            }
            if (transaction.getTotalInstallments() == null || transaction.getTotalInstallments() < 2) {
                throw new DomainException("Uma transação parcelada deve ter pelo menos 2 parcelas.");
            }
            if (transaction.getTotalInstallments() > 48) {
                throw new DomainException("O número máximo de parcelas é 48.");
            }
        }
    }

    private void validateTransactionForUpdate(Transaction transaction, Transaction existingTransaction) {
        BigDecimal amount = transaction.getAmount() != null ? transaction.getAmount() : existingTransaction.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor da transação deve ser maior que zero.");
        }

        String description = transaction.getDescription() != null ? transaction.getDescription() : existingTransaction.getDescription();
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("A descrição da transação é obrigatória.");
        }

        if (transaction.getCategoryId() != null) {
            if (!categoryRepositoryPort.existsByIdAndProfileId(transaction.getCategoryId(), existingTransaction.getProfileId())) {
                throw new ResourceNotFoundException("Categoria não encontrada.");
            }
        }
    }
}
