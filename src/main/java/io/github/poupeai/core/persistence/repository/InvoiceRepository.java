package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.domain.model.InvoiceNotificationData;
import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    Optional<InvoiceEntity> findByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year);
    
    List<InvoiceEntity> findByCreditCardId(UUID creditCardId);
    
    List<InvoiceEntity> findByCreditCardProfileUserId(UUID profileId);
    
    Optional<InvoiceEntity> findByIdAndCreditCardProfileUserId(UUID id, UUID profileId);
    
    boolean existsByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year);

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0)
        FROM InvoiceEntity i
        WHERE i.creditCard.id = :creditCardId
        AND i.status IN ('OPEN', 'CLOSED', 'OVERDUE')
        """)
    BigDecimal calculateUsedCreditLimit(@Param("creditCardId") UUID creditCardId);

    @Query("""
        SELECT i FROM InvoiceEntity i
        JOIN FETCH i.creditCard cc
        JOIN FETCH cc.profile p
        WHERE i.dueDate BETWEEN :startDate AND :endDate
        AND (i.status = 'OPEN' OR i.status = 'CLOSED')
        AND COALESCE(i.dueSoonNotificationSent, false) = false
        """)
    List<InvoiceEntity> findDueSoonNotNotified(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("""
        SELECT i FROM InvoiceEntity i
        JOIN FETCH i.creditCard cc
        JOIN FETCH cc.profile p
        WHERE i.dueDate < :today
        AND (i.status = 'OPEN' OR i.status = 'CLOSED')
        AND COALESCE(i.overdueNotificationSent, false) = false
        AND i.paidAmount < i.totalAmount
        """)
    List<InvoiceEntity> findOverdueNotNotified(
            @Param("today") LocalDate today);
    
    @Query("""
        SELECT new io.github.poupeai.core.domain.model.InvoiceNotificationData(
            i.id, cc.id, cc.name, i.month, i.year, i.dueDate, i.totalAmount, i.paidAmount,
            p.userId, p.email, CONCAT(p.firstName, ' ', p.lastName)
        )
        FROM InvoiceEntity i
        JOIN i.creditCard cc
        JOIN cc.profile p
        WHERE i.dueDate BETWEEN :startDate AND :endDate
        AND (i.status = 'OPEN' OR i.status = 'CLOSED')
        AND COALESCE(i.dueSoonNotificationSent, false) = false
        AND p.isDeactivated = false
        """)
    List<InvoiceNotificationData> findDueSoonNotificationsData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("""
        SELECT new io.github.poupeai.core.domain.model.InvoiceNotificationData(
            i.id, cc.id, cc.name, i.month, i.year, i.dueDate, i.totalAmount, i.paidAmount,
            p.userId, p.email, CONCAT(p.firstName, ' ', p.lastName)
        )
        FROM InvoiceEntity i
        JOIN i.creditCard cc
        JOIN cc.profile p
        WHERE i.dueDate < :today
        AND (i.status = 'OPEN' OR i.status = 'CLOSED')
        AND COALESCE(i.overdueNotificationSent, false) = false
        AND i.paidAmount < i.totalAmount
        AND p.isDeactivated = false
        """)
    List<InvoiceNotificationData> findOverdueNotificationsData(
            @Param("today") LocalDate today);
}
