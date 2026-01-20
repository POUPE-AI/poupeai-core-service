package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.InvoicePayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InvoicePaymentServicePort {
    InvoicePayment registerPayment(UUID invoiceId, UUID bankAccountId, BigDecimal amount, UUID profileId);
    void deletePayment(Long paymentId, UUID profileId);
    List<InvoicePayment> getPaymentsByInvoiceId(UUID invoiceId, UUID profileId);
    InvoicePayment getPaymentById(Long paymentId, UUID profileId);
}
