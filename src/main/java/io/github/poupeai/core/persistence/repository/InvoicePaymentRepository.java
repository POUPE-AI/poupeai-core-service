package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.InvoicePaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoicePaymentRepository extends JpaRepository<InvoicePaymentEntity, Long> {
    List<InvoicePaymentEntity> findByInvoiceId(UUID invoiceId);
    Optional<InvoicePaymentEntity> findByPaymentTransactionId(UUID paymentTransactionId);
    void deleteByInvoiceId(UUID invoiceId);
}
