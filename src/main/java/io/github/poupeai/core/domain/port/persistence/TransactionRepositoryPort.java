package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction create(Transaction transaction);
    
    Transaction update(Transaction transaction);
    
    Optional<Transaction> findById(UUID id);
    
    Optional<Transaction> findByIdAndProfileId(UUID id, UUID profileId);
    
    List<Transaction> findAllByProfileId(UUID profileId);
    
    void delete(UUID id);
    
    boolean existsByIdAndProfileId(UUID id, UUID profileId);
    
    List<Transaction> findByPurchaseGroupUuid(UUID purchaseGroupUuid);
    
    List<Transaction> findByInvoiceId(UUID invoiceId);
    
    List<Transaction> findByBankAccountId(UUID bankAccountId);
    
    BigDecimal sumAmountByBankAccountIdAndType(UUID bankAccountId, TransactionType type);
    
    BigDecimal sumAmountByInvoiceId(UUID invoiceId);
    
    void deleteByPurchaseGroupUuid(UUID purchaseGroupUuid);
    
    void deleteByInvoiceId(UUID invoiceId);
    
    List<Transaction> createAll(List<Transaction> transactions);

    PageDomain<Transaction> search(UUID profileId, TransactionFilter filter);
}
