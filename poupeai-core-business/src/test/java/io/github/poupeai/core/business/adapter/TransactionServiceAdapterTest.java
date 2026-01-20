package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceAdapterTest {

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private BankAccountRepositoryPort bankAccountRepositoryPort;

    @Mock
    private CreditCardRepositoryPort creditCardRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private InvoiceServicePort invoiceServicePort;

    @InjectMocks
    private TransactionServiceAdapter transactionServiceAdapter;

    @Nested
    @DisplayName("Create Transaction Tests")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create bank account transaction successfully")
        void shouldCreateBankAccountTransactionSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
            Transaction transaction = createBankAccountTransaction(profileId, bankAccountId, categoryId);

            when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)).thenReturn(true);
            when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)).thenReturn(Optional.of(category));
            when(transactionRepositoryPort.create(any())).thenReturn(transaction);

            Transaction result = transactionServiceAdapter.create(transaction);

            assertNotNull(result);
            assertEquals(transaction.getDescription(), result.getDescription());
            assertEquals(TransactionType.EXPENSE, result.getType());
            verify(transactionRepositoryPort).create(any());
        }

        @Test
        @DisplayName("Should create credit card transaction with automatic invoice creation")
        void shouldCreateCreditCardTransactionWithInvoice() {
            UUID profileId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
            Transaction transaction = createCreditCardTransaction(profileId, creditCardId, categoryId);
            CreditCard creditCard = createCreditCard(creditCardId, profileId);
            Invoice invoice = createInvoice(invoiceId, creditCardId);

            when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)).thenReturn(Optional.of(category));
            when(creditCardRepositoryPort.findByIdAndProfileId(creditCardId, profileId))
                    .thenReturn(Optional.of(creditCard));
            when(invoiceServicePort.getOrCreateInvoiceForDate(any(), any())).thenReturn(invoice);
            when(transactionRepositoryPort.create(any())).thenAnswer(inv -> {
                Transaction t = inv.getArgument(0);
                t.setId(UUID.randomUUID());
                return t;
            });

            Transaction result = transactionServiceAdapter.create(transaction);

            assertNotNull(result);
            assertEquals(invoiceId, result.getInvoiceId());
            assertEquals(TransactionType.EXPENSE, result.getType());
            verify(invoiceServicePort).updateInvoiceTotals(invoiceId);
        }

        @Test
        @DisplayName("Should create installment transactions correctly")
        void shouldCreateInstallmentTransactions() {
            UUID profileId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
            Transaction transaction = createInstallmentTransaction(profileId, creditCardId, categoryId, 3);
            CreditCard creditCard = createCreditCard(creditCardId, profileId);

            when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)).thenReturn(Optional.of(category));
            when(creditCardRepositoryPort.findByIdAndProfileId(creditCardId, profileId))
                    .thenReturn(Optional.of(creditCard));
            when(invoiceServicePort.getOrCreateInvoiceForDate(any(), any()))
                    .thenAnswer(inv -> createInvoice(UUID.randomUUID(), creditCardId));
            when(transactionRepositoryPort.createAll(anyList())).thenAnswer(inv -> {
                List<Transaction> transactions = inv.getArgument(0);
                transactions.forEach(t -> t.setId(UUID.randomUUID()));
                return transactions;
            });

            Transaction result = transactionServiceAdapter.create(transaction);

            assertNotNull(result);
            assertTrue(result.getIsInstallment());

            ArgumentCaptor<List<Transaction>> captor = ArgumentCaptor.forClass(List.class);
            verify(transactionRepositoryPort).createAll(captor.capture());
            List<Transaction> installments = captor.getValue();
            
            assertEquals(3, installments.size());
            assertEquals(1, installments.get(0).getInstallmentNumber());
            assertEquals(2, installments.get(1).getInstallmentNumber());
            assertEquals(3, installments.get(2).getInstallmentNumber());
        }

        @Test
        @DisplayName("Should throw exception when amount is zero or negative")
        void shouldThrowExceptionWhenAmountIsInvalid() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.ZERO)
                    .transactionDate(LocalDate.now())
                    .bankAccountId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when description is empty")
        void shouldThrowExceptionWhenDescriptionIsEmpty() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .bankAccountId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when neither bank account nor credit card is provided")
        void shouldThrowExceptionWhenNoPaymentMethodProvided() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .categoryId(UUID.randomUUID())
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when both bank account and credit card are provided")
        void shouldThrowExceptionWhenBothPaymentMethodsProvided() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .bankAccountId(UUID.randomUUID())
                    .creditCardId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Transaction transaction = createBankAccountTransaction(profileId, bankAccountId, categoryId);

            when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when credit card not found")
        void shouldThrowExceptionWhenCreditCardNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
            Transaction transaction = createCreditCardTransaction(profileId, creditCardId, categoryId);

            when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)).thenReturn(Optional.of(category));
            when(creditCardRepositoryPort.findByIdAndProfileId(creditCardId, profileId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when installment count is less than 2")
        void shouldThrowExceptionWhenInstallmentCountInvalid() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .creditCardId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .isInstallment(true)
                    .totalInstallments(1)
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when installment count exceeds 48")
        void shouldThrowExceptionWhenInstallmentCountExceedsMax() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .creditCardId(UUID.randomUUID())
                    .categoryId(UUID.randomUUID())
                    .isInstallment(true)
                    .totalInstallments(49)
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when trying to create installment with bank account")
        void shouldThrowExceptionWhenInstallmentWithBankAccount() {
            UUID profileId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .bankAccountId(bankAccountId)
                    .categoryId(UUID.randomUUID())
                    .isInstallment(true)
                    .totalInstallments(3)
                    .build();

            when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId)).thenReturn(true);

            DomainException exception = assertThrows(DomainException.class, 
                    () -> transactionServiceAdapter.create(transaction));
            assertEquals("Parcelamento só é permitido para transações de cartão de crédito.", 
                    exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when category is missing")
        void shouldThrowExceptionWhenCategoryIsMissing() {
            UUID profileId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .bankAccountId(UUID.randomUUID())
                    .build();

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }

        @Test
        @DisplayName("Should throw exception when credit card transaction has non-expense category")
        void shouldThrowExceptionWhenCreditCardTransactionHasIncomeCategory() {
            UUID profileId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = createCategory(categoryId, profileId, CategoryType.INCOME);
            Transaction transaction = createCreditCardTransaction(profileId, creditCardId, categoryId);

            when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)).thenReturn(Optional.of(category));

            assertThrows(DomainException.class, () -> transactionServiceAdapter.create(transaction));
        }
    }

    @Nested
    @DisplayName("Update Transaction Tests")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Should update transaction successfully")
        void shouldUpdateTransactionSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID bankAccountId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
            
            Transaction existingTransaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .description("Original")
                    .amount(BigDecimal.valueOf(100))
                    .transactionDate(LocalDate.now())
                    .bankAccountId(bankAccountId)
                    .categoryId(categoryId)
                    .type(TransactionType.EXPENSE)
                    .isInstallment(false)
                    .build();

            Transaction updatedTransaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .description("Updated")
                    .amount(BigDecimal.valueOf(200))
                    .transactionDate(LocalDate.now())
                    .categoryId(categoryId)
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(existingTransaction));
            when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId)).thenReturn(Optional.of(category));
            when(categoryRepositoryPort.existsByIdAndProfileId(categoryId, profileId)).thenReturn(true);
            when(transactionRepositoryPort.update(any())).thenReturn(updatedTransaction);

            Transaction result = transactionServiceAdapter.update(updatedTransaction, profileId);

            assertNotNull(result);
            assertEquals("Updated", result.getDescription());
            verify(transactionRepositoryPort).update(any());
        }

        @Test
        @DisplayName("Should throw exception when updating installment transaction")
        void shouldThrowExceptionWhenUpdatingInstallmentTransaction() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            
            Transaction existingTransaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .isInstallment(true)
                    .purchaseGroupUuid(UUID.randomUUID())
                    .build();

            Transaction updateRequest = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .description("Updated")
                    .amount(BigDecimal.valueOf(200))
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(existingTransaction));

            assertThrows(DomainException.class, 
                    () -> transactionServiceAdapter.update(updateRequest, profileId));
        }

        @Test
        @DisplayName("Should throw exception when transaction not found for update")
        void shouldThrowExceptionWhenTransactionNotFoundForUpdate() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .description("Test")
                    .amount(BigDecimal.valueOf(100))
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> transactionServiceAdapter.update(transaction, profileId));
        }
    }

    @Nested
    @DisplayName("Delete Transaction Tests")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete single transaction successfully")
        void shouldDeleteSingleTransactionSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .isInstallment(false)
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(transaction));

            transactionServiceAdapter.delete(transactionId, profileId);

            verify(transactionRepositoryPort).delete(transactionId);
        }

        @Test
        @DisplayName("Should delete all installments when deleting installment transaction")
        void shouldDeleteAllInstallmentsWhenDeletingInstallment() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID purchaseGroupUuid = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .isInstallment(true)
                    .purchaseGroupUuid(purchaseGroupUuid)
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(transaction));

            transactionServiceAdapter.delete(transactionId, profileId);

            verify(transactionRepositoryPort).deleteByPurchaseGroupUuid(purchaseGroupUuid);
            verify(transactionRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("Should update invoice totals after deleting transaction with invoice")
        void shouldUpdateInvoiceTotalsAfterDeletion() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .invoiceId(invoiceId)
                    .isInstallment(false)
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(transaction));

            transactionServiceAdapter.delete(transactionId, profileId);

            verify(invoiceServicePort).updateInvoiceTotals(invoiceId);
        }

        @Test
        @DisplayName("Should throw exception when transaction not found for delete")
        void shouldThrowExceptionWhenTransactionNotFoundForDelete() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> transactionServiceAdapter.delete(transactionId, profileId));
        }
    }

    @Nested
    @DisplayName("Find Transaction Tests")
    class FindTransactionTests {

        @Test
        @DisplayName("Should find transaction by ID successfully")
        void shouldFindTransactionByIdSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = Transaction.builder()
                    .id(transactionId)
                    .profileId(profileId)
                    .description("Test")
                    .build();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.of(transaction));

            Transaction result = transactionServiceAdapter.findByIdAndProfileId(transactionId, profileId);

            assertNotNull(result);
            assertEquals(transactionId, result.getId());
        }

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();

            when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> transactionServiceAdapter.findByIdAndProfileId(transactionId, profileId));
        }

        @Test
        @DisplayName("Should find all transactions by profile ID")
        void shouldFindAllTransactionsByProfileId() {
            UUID profileId = UUID.randomUUID();
            List<Transaction> transactions = List.of(
                    Transaction.builder().id(UUID.randomUUID()).description("Trans 1").build(),
                    Transaction.builder().id(UUID.randomUUID()).description("Trans 2").build()
            );

            when(transactionRepositoryPort.findAllByProfileId(profileId)).thenReturn(transactions);

            List<Transaction> result = transactionServiceAdapter.findAllByProfileId(profileId);

            assertEquals(2, result.size());
        }
    }

    private Transaction createBankAccountTransaction(UUID profileId, UUID bankAccountId, UUID categoryId) {
        return Transaction.builder()
                .profileId(profileId)
                .description("Test Transaction")
                .amount(BigDecimal.valueOf(100))
                .transactionDate(LocalDate.now())
                .bankAccountId(bankAccountId)
                .categoryId(categoryId)
                .build();
    }

    private Transaction createCreditCardTransaction(UUID profileId, UUID creditCardId, UUID categoryId) {
        return Transaction.builder()
                .profileId(profileId)
                .description("Test Credit Card Transaction")
                .amount(BigDecimal.valueOf(100))
                .transactionDate(LocalDate.now())
                .creditCardId(creditCardId)
                .categoryId(categoryId)
                .isInstallment(false)
                .build();
    }

    private Transaction createInstallmentTransaction(UUID profileId, UUID creditCardId, UUID categoryId, int installments) {
        return Transaction.builder()
                .profileId(profileId)
                .description("Installment Purchase")
                .amount(BigDecimal.valueOf(300))
                .transactionDate(LocalDate.now())
                .creditCardId(creditCardId)
                .categoryId(categoryId)
                .isInstallment(true)
                .totalInstallments(installments)
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

    private CreditCard createCreditCard(UUID id, UUID profileId) {
        return CreditCard.builder()
                .id(id)
                .profileId(profileId)
                .name("Test Card")
                .closingDay(10)
                .dueDay(20)
                .creditLimit(BigDecimal.valueOf(5000))
                .build();
    }

    private Invoice createInvoice(UUID id, UUID creditCardId) {
        return Invoice.builder()
                .id(id)
                .creditCardId(creditCardId)
                .month(LocalDate.now().getMonthValue())
                .year(LocalDate.now().getYear())
                .closingDate(LocalDate.now().plusDays(10))
                .dueDate(LocalDate.now().plusDays(20))
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .build();
    }
}
