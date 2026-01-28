package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository
                extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {
        Optional<TransactionEntity> findByIdAndProfileUserId(UUID id, UUID profileId);

        List<TransactionEntity> findAllByProfileUserId(UUID profileId);

        List<TransactionEntity> findAllByProfileUserIdOrderByTransactionDateDesc(UUID profileId);

        boolean existsByIdAndProfileUserId(UUID id, UUID profileId);

        List<TransactionEntity> findByPurchaseGroupUuid(UUID purchaseGroupUuid);

        List<TransactionEntity> findByInvoiceId(UUID invoiceId);

        List<TransactionEntity> findByBankAccountId(UUID bankAccountId);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.bankAccount.id = :bankAccountId AND t.type = :type")
        BigDecimal sumAmountByBankAccountIdAndType(@Param("bankAccountId") UUID bankAccountId,
                        @Param("type") TransactionType type);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.invoice.id = :invoiceId")
        BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);

        void deleteByPurchaseGroupUuid(UUID purchaseGroupUuid);

        void deleteByInvoiceId(UUID invoiceId);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t " +
                        "WHERE t.profile.userId = :profileId AND t.type = :type " +
                        "AND t.bankAccount IS NOT NULL " +
                        "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
        BigDecimal sumAmountByProfileIdAndTypeAndDateRange(
                        @Param("profileId") UUID profileId,
                        @Param("type") TransactionType type,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t " +
                        "WHERE t.profile.userId = :profileId AND t.type = :type " +
                        "AND t.bankAccount IS NOT NULL " +
                        "AND t.transactionDate < :date")
        BigDecimal sumAmountByProfileIdAndTypeBeforeDate(
                        @Param("profileId") UUID profileId,
                        @Param("type") TransactionType type,
                        @Param("date") LocalDate date);

        @Query("SELECT t FROM TransactionEntity t " +
                        "WHERE t.profile.userId = :profileId " +
                        "AND t.bankAccount IS NOT NULL " +
                        "AND t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
                        "ORDER BY t.transactionDate ASC")
        List<TransactionEntity> findByProfileIdAndBankAccountNotNullAndDateRange(
                        @Param("profileId") UUID profileId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
