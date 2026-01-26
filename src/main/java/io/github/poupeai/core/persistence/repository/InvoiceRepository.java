package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
