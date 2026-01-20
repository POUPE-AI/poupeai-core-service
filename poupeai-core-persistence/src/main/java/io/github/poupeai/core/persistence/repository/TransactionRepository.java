package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByIdAndProfileUserId(UUID id, UUID profileId);
    
    List<TransactionEntity> findAllByProfileUserId(UUID profileId);
    
    List<TransactionEntity> findAllByProfileUserIdOrderByTransactionDateDesc(UUID profileId);
    
    boolean existsByIdAndProfileUserId(UUID id, UUID profileId);
    
    List<TransactionEntity> findByPurchaseGroupUuid(UUID purchaseGroupUuid);
    
    List<TransactionEntity> findByInvoiceId(UUID invoiceId);
    
    List<TransactionEntity> findByBankAccountId(UUID bankAccountId);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.bankAccount.id = :bankAccountId AND t.type = :type")
    BigDecimal sumAmountByBankAccountIdAndType(@Param("bankAccountId") UUID bankAccountId, @Param("type") TransactionType type);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.invoice.id = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
    
    void deleteByPurchaseGroupUuid(UUID purchaseGroupUuid);
    
    void deleteByInvoiceId(UUID invoiceId);
}
