package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InvoicePaymentRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoicePaymentServiceAdapterTest {

    @Mock
    private InvoicePaymentRepositoryPort invoicePaymentRepositoryPort;

    @Mock
    private InvoiceServicePort invoiceServicePort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private BankAccountRepositoryPort bankAccountRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private InvoicePaymentServiceAdapter invoicePaymentServiceAdapter;

    @Nested
    @DisplayName("Register Payment Tests")
    class RegisterPaymentTests {

        @Test
        @DisplayName("Should register payment successfully")
        void shouldRegisterPaymentSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);

            Invoice invoice = createInvoice(invoiceId, 1, 2026);
            Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
            Transaction savedTransaction = createTransaction(UUID.randomUUID(), profileId, bankAccountId);
            InvoicePayment savedPayment = createInvoicePayment(1L, invoiceId, savedTransaction.getId(), amount);

            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);
            when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)).thenReturn(true);
            when(categoryRepositoryPort.findAllByProfileId(profileId)).thenReturn(List.of(category));
            when(transactionRepositoryPort.create(any())).thenReturn(savedTransaction);
            when(invoicePaymentRepositoryPort.create(any())).thenReturn(savedPayment);

            InvoicePayment result = invoicePaymentServiceAdapter.registerPayment(
                    invoiceId, bankAccountId, amount, profileId
            );

            assertNotNull(result);
            assertEquals(invoiceId, result.getInvoiceId());
            assertEquals(amount, result.getAmount());
            
            verify(transactionRepositoryPort).create(any());
            verify(invoicePaymentRepositoryPort).create(any());
            verify(invoiceServicePort).addPaymentToInvoice(invoiceId, amount);
        }

        @Test
        @DisplayName("Should throw exception when amount is zero or negative")
        void shouldThrowExceptionWhenAmountIsInvalid() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();

            assertThrows(DomainException.class, () -> 
                invoicePaymentServiceAdapter.registerPayment(invoiceId, bankAccountId, BigDecimal.ZERO, profileId)
            );
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void shouldThrowExceptionWhenInvoiceNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);

            when(invoiceServicePort.findById(invoiceId, profileId))
                    .thenThrow(new ResourceNotFoundException("Fatura não encontrada."));

            assertThrows(ResourceNotFoundException.class, () -> 
                invoicePaymentServiceAdapter.registerPayment(invoiceId, bankAccountId, amount, profileId)
            );
        }

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);

            Invoice invoice = createInvoice(invoiceId, 1, 2026);

            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);
            when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> 
                invoicePaymentServiceAdapter.registerPayment(invoiceId, bankAccountId, amount, profileId)
            );
        }

        @Test
        @DisplayName("Should throw exception when no expense category found")
        void shouldThrowExceptionWhenNoExpenseCategoryFound() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);

            Invoice invoice = createInvoice(invoiceId, 1, 2026);

            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);
            when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)).thenReturn(true);
            when(categoryRepositoryPort.findAllByProfileId(profileId)).thenReturn(List.of());

            assertThrows(DomainException.class, () -> 
                invoicePaymentServiceAdapter.registerPayment(invoiceId, bankAccountId, amount, profileId)
            );
        }

        @Test
        @DisplayName("Should throw exception when invoice is already paid")
        void shouldThrowExceptionWhenInvoiceAlreadyPaid() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(500);

            Invoice invoice = createInvoice(invoiceId, 1, 2026, InvoiceStatus.PAID);

            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);

            DomainException exception = assertThrows(DomainException.class, () -> 
                invoicePaymentServiceAdapter.registerPayment(invoiceId, bankAccountId, amount, profileId)
            );
            assertEquals("Esta fatura já está paga.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete Payment Tests")
    class DeletePaymentTests {

        @Test
        @DisplayName("Should delete payment successfully")
        void shouldDeletePaymentSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            Long paymentId = 1L;
            BigDecimal amount = BigDecimal.valueOf(500);

            InvoicePayment payment = createInvoicePayment(paymentId, invoiceId, transactionId, amount);
            Invoice invoice = createInvoice(invoiceId, 1, 2026, InvoiceStatus.PAID);
            Transaction transaction = createTransaction(transactionId, profileId, UUID.randomUUID());

            when(invoicePaymentRepositoryPort.findById(paymentId)).thenReturn(Optional.of(payment));
            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);
            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(transaction));

            invoicePaymentServiceAdapter.deletePayment(paymentId, profileId);

            verify(invoicePaymentRepositoryPort).delete(paymentId);
            verify(transactionRepositoryPort).delete(transactionId);
            verify(invoiceServicePort).removePaymentFromInvoice(invoiceId, amount);
        }

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            UUID profileId = UUID.randomUUID();
            Long paymentId = 1L;

            when(invoicePaymentRepositoryPort.findById(paymentId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> 
                invoicePaymentServiceAdapter.deletePayment(paymentId, profileId)
            );
        }

        @Test
        @DisplayName("Should throw exception when trying to delete payment from non-paid invoice")
        void shouldThrowExceptionWhenDeletingPaymentFromNonPaidInvoice() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            Long paymentId = 1L;
            BigDecimal amount = BigDecimal.valueOf(500);

            InvoicePayment payment = createInvoicePayment(paymentId, invoiceId, transactionId, amount);
            Invoice invoice = createInvoice(invoiceId, 1, 2026, InvoiceStatus.OPEN);

            when(invoicePaymentRepositoryPort.findById(paymentId)).thenReturn(Optional.of(payment));
            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);

            DomainException exception = assertThrows(DomainException.class, () -> 
                invoicePaymentServiceAdapter.deletePayment(paymentId, profileId)
            );
            assertEquals("Esta fatura não possui pagamentos registrados.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Payments Tests")
    class GetPaymentsTests {

        @Test
        @DisplayName("Should get payments by invoice ID")
        void shouldGetPaymentsByInvoiceId() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(invoiceId, 1, 2026);
            List<InvoicePayment> payments = List.of(
                    createInvoicePayment(1L, invoiceId, UUID.randomUUID(), BigDecimal.valueOf(100)),
                    createInvoicePayment(2L, invoiceId, UUID.randomUUID(), BigDecimal.valueOf(200))
            );

            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);
            when(invoicePaymentRepositoryPort.findByInvoiceId(invoiceId)).thenReturn(payments);

            List<InvoicePayment> result = invoicePaymentServiceAdapter.getPaymentsByInvoiceId(invoiceId, profileId);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should get payment by ID")
        void shouldGetPaymentById() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Long paymentId = 1L;
            InvoicePayment payment = createInvoicePayment(paymentId, invoiceId, UUID.randomUUID(), BigDecimal.valueOf(500));
            Invoice invoice = createInvoice(invoiceId, 1, 2026);

            when(invoicePaymentRepositoryPort.findById(paymentId)).thenReturn(Optional.of(payment));
            when(invoiceServicePort.findById(invoiceId, profileId)).thenReturn(invoice);

            InvoicePayment result = invoicePaymentServiceAdapter.getPaymentById(paymentId, profileId);

            assertNotNull(result);
            assertEquals(paymentId, result.getId());
        }
    }

    private Invoice createInvoice(UUID id, int month, int year) {
        return createInvoice(id, month, year, InvoiceStatus.OPEN);
    }

    private Invoice createInvoice(UUID id, int month, int year, InvoiceStatus status) {
        return Invoice.builder()
                .id(id)
                .creditCardId(UUID.randomUUID())
                .month(month)
                .year(year)
                .closingDate(LocalDate.of(year, month, 10))
                .dueDate(LocalDate.of(year, month, 20))
                .totalAmount(BigDecimal.valueOf(1000))
                .paidAmount(status == InvoiceStatus.PAID ? BigDecimal.valueOf(1000) : BigDecimal.ZERO)
                .status(status)
                .build();
    }

    private Category createCategory(UUID id, UUID profileId, CategoryType type) {
        return Category.builder()
                .id(id)
                .profileId(profileId)
                .name("Test Category")
                .type(type)
                .build();
    }

    private Transaction createTransaction(UUID id, UUID profileId, UUID bankAccountId) {
        return Transaction.builder()
                .id(id)
                .profileId(profileId)
                .bankAccountId(bankAccountId)
                .description("Payment Transaction")
                .amount(BigDecimal.valueOf(500))
                .transactionDate(LocalDate.now())
                .build();
    }

    private InvoicePayment createInvoicePayment(Long id, UUID invoiceId, UUID transactionId, BigDecimal amount) {
        return InvoicePayment.builder()
                .id(id)
                .invoiceId(invoiceId)
                .paymentTransactionId(transactionId)
                .amount(amount)
                .build();
    }
}
