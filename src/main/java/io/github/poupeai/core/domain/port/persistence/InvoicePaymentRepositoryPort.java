package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.InvoicePayment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoicePaymentRepositoryPort {
    InvoicePayment create(InvoicePayment invoicePayment);
    Optional<InvoicePayment> findById(Long id);
    Optional<InvoicePayment> findByPaymentTransactionId(UUID paymentTransactionId);
    List<InvoicePayment> findByInvoiceId(UUID invoiceId);
    void delete(Long id);
    void deleteByInvoiceId(UUID invoiceId);
}
