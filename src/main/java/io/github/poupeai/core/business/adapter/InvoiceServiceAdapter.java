package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceAdapter implements InvoiceServicePort {
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Override
    @Transactional
    public Invoice getOrCreateInvoiceForDate(CreditCard creditCard, LocalDate transactionDate) {
        int[] monthYear = calculateInvoiceMonthYear(creditCard.getClosingDay(), transactionDate);
        int invoiceMonth = monthYear[0];
        int invoiceYear = monthYear[1];

        return invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(
                creditCard.getId(), invoiceMonth, invoiceYear
        ).orElseGet(() -> createInvoice(creditCard, invoiceMonth, invoiceYear));
    }

    @Override
    public Invoice findById(UUID id, UUID profileId) {
        return invoiceRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));
    }

    @Override
    public List<Invoice> findByCreditCardId(UUID creditCardId, UUID profileId) {
        return invoiceRepositoryPort.findByCreditCardId(creditCardId);
    }

    @Override
    public List<Invoice> findByProfileId(UUID profileId) {
        return invoiceRepositoryPort.findByProfileId(profileId);
    }

    @Override
    @Transactional
    public void updateInvoiceTotals(UUID invoiceId) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        BigDecimal totalAmount = transactionRepositoryPort.sumAmountByInvoiceId(invoiceId);
        invoice.setTotalAmount(totalAmount);

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

        return invoiceRepositoryPort.create(invoice);
    }

    private LocalDate calculateClosingDate(int closingDay, int month, int year) {
        int maxDaysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        int actualClosingDay = Math.min(closingDay, maxDaysInMonth);
        return LocalDate.of(year, month, actualClosingDay);
    }

    private LocalDate calculateDueDate(int dueDay, LocalDate closingDate) {
        LocalDate nextMonth = closingDate.plusMonths(1);
        int maxDaysInMonth = nextMonth.lengthOfMonth();
        int actualDueDay = Math.min(dueDay, maxDaysInMonth);
        return LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), actualDueDay);
    }

    @Override
    @Transactional
    public void addPaymentToInvoice(UUID invoiceId, BigDecimal amount) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);
        
        updateInvoiceStatus(invoice);
        
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
        
        updateInvoiceStatus(invoice);
        
        invoiceRepositoryPort.update(invoice);
    }

    private void updateInvoiceStatus(Invoice invoice) {
        BigDecimal totalAmount = invoice.getTotalAmount();
        BigDecimal paidAmount = invoice.getPaidAmount();
        LocalDate today = LocalDate.now();

        if (paidAmount.compareTo(totalAmount) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        } else if (invoice.getDueDate().isBefore(today)) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        } else if (invoice.getClosingDate().isBefore(today) || invoice.getClosingDate().isEqual(today)) {
            invoice.setStatus(InvoiceStatus.CLOSED);
        } else {
            invoice.setStatus(InvoiceStatus.OPEN);
        }
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
}
