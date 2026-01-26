package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.InvoicePaymentServicePort;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InvoicePaymentRepositoryPort;
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
public class InvoicePaymentServiceAdapter implements InvoicePaymentServicePort {
    private final InvoicePaymentRepositoryPort invoicePaymentRepositoryPort;
    private final InvoiceServicePort invoiceServicePort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;

    @Override
    @Transactional
    public InvoicePayment registerPayment(UUID invoiceId, UUID bankAccountId, BigDecimal amount, UUID profileId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor do pagamento deve ser maior que zero.");
        }

        Invoice invoice = invoiceServicePort.findById(invoiceId, profileId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new DomainException("Esta fatura já está paga.");
        }

        if (!bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)) {
            throw new ResourceNotFoundException("Conta bancária não encontrada.");
        }

        Category expenseCategory = findOrCreatePaymentCategory(profileId);

        Transaction paymentTransaction = Transaction.builder()
                .profileId(profileId)
                .description(String.format("Pagamento fatura %02d/%d", invoice.getMonth(), invoice.getYear()))
                .amount(amount)
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.now())
                .bankAccountId(bankAccountId)
                .categoryId(expenseCategory.getId())
                .isInstallment(false)
                .build();

        Transaction savedTransaction = transactionRepositoryPort.create(paymentTransaction);

        InvoicePayment invoicePayment = InvoicePayment.builder()
                .invoiceId(invoiceId)
                .paymentTransactionId(savedTransaction.getId())
                .amount(amount)
                .build();

        InvoicePayment savedPayment = invoicePaymentRepositoryPort.create(invoicePayment);

        invoiceServicePort.addPaymentToInvoice(invoiceId, amount);

        return savedPayment;
    }

    @Override
    @Transactional
    public void deletePayment(Long paymentId, UUID profileId) {
        InvoicePayment payment = invoicePaymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado."));

        Invoice invoice = invoiceServicePort.findById(payment.getInvoiceId(), profileId);

        if (invoice.getStatus() != InvoiceStatus.PAID && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new DomainException("Esta fatura não possui pagamentos registrados.");
        }

        if (transactionRepositoryPort.findByIdAndProfileId(payment.getPaymentTransactionId(), profileId).isEmpty()) {
            throw new ResourceNotFoundException("Transação de pagamento não encontrada.");
        }

        BigDecimal paymentAmount = payment.getAmount();

        invoicePaymentRepositoryPort.delete(paymentId);

        transactionRepositoryPort.delete(payment.getPaymentTransactionId());

        invoiceServicePort.removePaymentFromInvoice(payment.getInvoiceId(), paymentAmount);
    }

    @Override
    public List<InvoicePayment> getPaymentsByInvoiceId(UUID invoiceId, UUID profileId) {
        invoiceServicePort.findById(invoiceId, profileId);
        
        return invoicePaymentRepositoryPort.findByInvoiceId(invoiceId);
    }

    @Override
    public InvoicePayment getPaymentById(Long paymentId, UUID profileId) {
        InvoicePayment payment = invoicePaymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado."));

        invoiceServicePort.findById(payment.getInvoiceId(), profileId);

        return payment;
    }

    private Category findOrCreatePaymentCategory(UUID profileId) {
        List<Category> categories = categoryRepositoryPort.findAllByProfileId(profileId);
        
        return categories.stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Nenhuma categoria de despesa encontrada. Crie uma categoria de despesa antes de pagar faturas."
                ));
    }
}
