package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceFilter;
import io.github.poupeai.core.domain.model.PageDomain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceServicePort {
    Invoice getOrCreateInvoiceForDate(CreditCard creditCard, LocalDate transactionDate);
    Invoice findById(UUID id, UUID profileId);
    List<Invoice> findByCreditCardId(UUID creditCardId, UUID profileId);
    List<Invoice> findByProfileId(UUID profileId);
    void updateInvoiceTotals(UUID invoiceId);
    void addPaymentToInvoice(UUID invoiceId, BigDecimal amount);
    void removePaymentFromInvoice(UUID invoiceId, BigDecimal amount);
    void deleteInvoice(UUID invoiceId, UUID profileId);
    PageDomain<Invoice> search(UUID profileId, InvoiceFilter filter);
}
