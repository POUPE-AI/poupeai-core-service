package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepositoryPort {
    Invoice create(Invoice invoice);
    
    Invoice update(Invoice invoice);
    
    Optional<Invoice> findById(UUID id);
    
    Optional<Invoice> findByIdAndProfileId(UUID id, UUID profileId);
    
    Optional<Invoice> findByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year);
    
    List<Invoice> findByCreditCardId(UUID creditCardId);
    
    List<Invoice> findByProfileId(UUID profileId);
    
    void delete(UUID id);
    
    boolean existsByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year);
}
