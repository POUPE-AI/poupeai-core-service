package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceFilter;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceAdapter implements InvoiceServicePort {
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Override
    @Transactional
    public Invoice getOrCreateInvoiceForDate(CreditCard creditCard, LocalDate transactionDate) {
        int[] monthYear = calculateInvoiceMonthYear(creditCard.getClosingDay(), transactionDate);
        int invoiceMonth = monthYear[0];
        int invoiceYear = monthYear[1];

        return invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(creditCard.getId(), invoiceMonth, invoiceYear)
                .map(invoice -> {
                    syncInvoiceDatesIfChanged(invoice, creditCard);
                    invoice.updateStatusFromDates(LocalDate.now());
                    return invoiceRepositoryPort.update(invoice);
                })
                .orElseGet(() -> createInvoice(creditCard, invoiceMonth, invoiceYear));
    }

    @Override
    public Invoice findById(UUID id, UUID profileId) {
        Invoice invoice = invoiceRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada"));

        invoice.updateStatusFromDates(LocalDate.now());
        return invoice;
    }

    @Override
    public List<Invoice> findByCreditCardId(UUID creditCardId, UUID profileId) {
        List<Invoice> invoices = invoiceRepositoryPort.findByCreditCardId(creditCardId);
        invoices.forEach(invoice -> invoice.updateStatusFromDates(LocalDate.now()));
        return invoices;
    }

    @Override
    public List<Invoice> findByProfileId(UUID profileId) {
        List<Invoice> invoices = invoiceRepositoryPort.findByProfileId(profileId);
        invoices.forEach(invoice -> invoice.updateStatusFromDates(LocalDate.now()));
        return invoices;
    }

    @Override
    @Transactional
    public void updateInvoiceTotals(UUID invoiceId) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        BigDecimal totalAmount = transactionRepositoryPort.sumAmountByInvoiceId(invoiceId);
        invoice.setTotalAmount(totalAmount);

        invoice.updateStatusFromDates(LocalDate.now());

        invoiceRepositoryPort.update(invoice);
    }

    private int[] calculateInvoiceMonthYear(int closingDay, LocalDate transactionDate) {
        int dayOfMonth = transactionDate.getDayOfMonth();
        int month = transactionDate.getMonthValue();
        int year = transactionDate.getYear();

        if (dayOfMonth > closingDay) {
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }

        return new int[]{month, year};
    }

    private Invoice createInvoice(CreditCard creditCard, int month, int year) {
        LocalDate closingDate = calculateClosingDate(creditCard.getClosingDay(), month, year);
        LocalDate dueDate = calculateDueDate(creditCard.getDueDay(), closingDate);

        Invoice invoice = Invoice.builder()
                .creditCardId(creditCard.getId())
                .month(month)
                .year(year)
                .closingDate(closingDate)
                .dueDate(dueDate)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .dueSoonNotificationSent(false)
                .overdueNotificationSent(false)
                .build();

        invoice.updateStatusFromDates(LocalDate.now());
        return invoiceRepositoryPort.create(invoice);
    }

    private void syncInvoiceDatesIfChanged(Invoice invoice, CreditCard creditCard) {
        LocalDate expectedClosingDate = calculateClosingDate(creditCard.getClosingDay(), invoice.getMonth(), invoice.getYear());
        LocalDate expectedDueDate = calculateDueDate(creditCard.getDueDay(), expectedClosingDate);

        boolean changed = false;

        if (!invoice.getClosingDate().isEqual(expectedClosingDate)) {
            invoice.setClosingDate(expectedClosingDate);
            changed = true;
        }

        if (!invoice.getDueDate().isEqual(expectedDueDate)) {
            invoice.setDueDate(expectedDueDate);
            changed = true;
        }

        if (changed) {
            log.info("Corrigindo datas da fatura {}: Fechamento {} -> {}, Vencimento {} -> {}",
                    invoice.getId(), invoice.getClosingDate(), expectedClosingDate, invoice.getDueDate(), expectedDueDate);
        }
    }

    private LocalDate calculateClosingDate(int closingDay, int month, int year) {
        int maxDaysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        int actualClosingDay = Math.min(closingDay, maxDaysInMonth);
        return LocalDate.of(year, month, actualClosingDay);
    }

    private LocalDate calculateDueDate(int dueDay, LocalDate closingDate) {
        LocalDate baseDate = closingDate;

        if (dueDay <= closingDate.getDayOfMonth()) {
            baseDate = baseDate.plusMonths(1);
        }

        int maxDaysInMonth = baseDate.lengthOfMonth();
        int actualDueDay = Math.min(dueDay, maxDaysInMonth);

        return baseDate.withDayOfMonth(actualDueDay);
    }

    @Override
    @Transactional
    public void addPaymentToInvoice(UUID invoiceId, BigDecimal amount) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);

        invoice.updateStatusFromDates(LocalDate.now());

        invoiceRepositoryPort.update(invoice);
    }

    @Override
    @Transactional
    public void removePaymentFromInvoice(UUID invoiceId, BigDecimal amount) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        BigDecimal newPaidAmount = invoice.getPaidAmount().subtract(amount);
        if (newPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            newPaidAmount = BigDecimal.ZERO;
        }
        invoice.setPaidAmount(newPaidAmount);

        invoice.updateStatusFromDates(LocalDate.now());

        invoiceRepositoryPort.update(invoice);
    }

    @Override
    @Transactional
    public void deleteInvoice(UUID invoiceId, UUID profileId) {
        Invoice invoice = invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID) {
            throw new DomainException("Não é possível deletar uma fatura com pagamentos registrados. Estorne os pagamentos primeiro.");
        }

        transactionRepositoryPort.deleteByInvoiceId(invoiceId);
        
        invoiceRepositoryPort.delete(invoiceId);
    }

    @Override
    @Transactional
    public PageDomain<Invoice> search(UUID profileId, InvoiceFilter filter) {
        if (filter.getSortBy() == null || filter.getSortBy().isEmpty()) {
            filter.setSortBy("dueDate");
        }

        PageDomain<Invoice> page = invoiceRepositoryPort.search(profileId, filter);
        page.getContent().forEach(invoice -> {
            invoice.updateStatusFromDates(LocalDate.now());
        });

        return page;
    }
}
