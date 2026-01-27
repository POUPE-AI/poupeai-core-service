package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.output.StoragePort;
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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        @Mock
        private StoragePort storagePort;

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
                        Transaction inputTransaction = createBankAccountTransaction(profileId, bankAccountId, categoryId);

                        when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId))
                                .thenReturn(Optional.of(category));
                        when(bankAccountRepositoryPort.existsByIdAndProfileId(bankAccountId, profileId))
                                .thenReturn(true);
                        when(transactionRepositoryPort.create(any(Transaction.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                        Transaction result = transactionServiceAdapter.create(inputTransaction);

                        assertNotNull(result);
                        assertEquals(TransactionType.EXPENSE, result.getType());
                        assertEquals("Test Category", result.getCategory().getName());
                        verify(transactionRepositoryPort).create(any(Transaction.class));
                }

                @Test
                @DisplayName("Should create credit card transaction (single installment) with invoice")
                void shouldCreateCreditCardTransactionWithInvoice() {
                        UUID profileId = UUID.randomUUID();
                        UUID creditCardId = UUID.randomUUID();
                        UUID invoiceId = UUID.randomUUID();
                        UUID categoryId = UUID.randomUUID();

                        Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
                        Transaction inputTransaction = createCreditCardTransaction(profileId, creditCardId, categoryId);
                        CreditCard creditCard = createCreditCard(creditCardId, profileId);
                        Invoice invoice = createInvoice(invoiceId, creditCardId);

                        when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId))
                                .thenReturn(Optional.of(category));
                        when(creditCardRepositoryPort.findByIdAndProfileId(creditCardId, profileId))
                                .thenReturn(Optional.of(creditCard));
                        when(invoiceServicePort.getOrCreateInvoiceForDate(any(CreditCard.class), any(LocalDate.class)))
                                .thenReturn(invoice);

                        when(transactionRepositoryPort.createAll(anyList()))
                                .thenAnswer(inv -> inv.getArgument(0));

                        Transaction result = transactionServiceAdapter.create(inputTransaction);

                        assertNotNull(result);
                        assertEquals(invoiceId, result.getInvoiceId());

                        verify(invoiceServicePort).updateInvoiceTotals(invoiceId);
                        verify(transactionRepositoryPort).createAll(anyList());
                }

                @Test
                @DisplayName("Should create installment transactions correctly")
                void shouldCreateInstallmentTransactions() {
                        UUID profileId = UUID.randomUUID();
                        UUID creditCardId = UUID.randomUUID();
                        UUID categoryId = UUID.randomUUID();

                        Category category = createCategory(categoryId, profileId, CategoryType.EXPENSE);
                        Transaction inputTransaction = createInstallmentTransaction(profileId, creditCardId, categoryId, 3);
                        CreditCard creditCard = createCreditCard(creditCardId, profileId);
                        Invoice invoice = createInvoice(UUID.randomUUID(), creditCardId);

                        when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId))
                                .thenReturn(Optional.of(category));
                        when(creditCardRepositoryPort.findByIdAndProfileId(creditCardId, profileId))
                                .thenReturn(Optional.of(creditCard));
                        when(invoiceServicePort.getOrCreateInvoiceForDate(any(), any()))
                                .thenReturn(invoice);
                        when(transactionRepositoryPort.createAll(anyList()))
                                .thenAnswer(inv -> inv.getArgument(0));

                        Transaction result = transactionServiceAdapter.create(inputTransaction);

                        ArgumentCaptor<List<Transaction>> listCaptor = ArgumentCaptor.forClass(List.class);
                        verify(transactionRepositoryPort).createAll(listCaptor.capture());

                        List<Transaction> capturedList = listCaptor.getValue();
                        assertEquals(3, capturedList.size());
                        assertEquals("Installment Purchase (1/3)", capturedList.get(0).getDescription());
                        assertEquals(1, capturedList.get(0).getInstallmentNumber());
                        assertEquals(3, capturedList.get(2).getInstallmentNumber());
                        assertNotNull(capturedList.get(0).getPurchaseGroupUuid());
                }

                @Test
                @DisplayName("Should throw exception when validations fail (e.g. invalid amount)")
                void shouldThrowExceptionWhenValidationFails() {
                        UUID profileId = UUID.randomUUID();
                        UUID categoryId = UUID.randomUUID();

                        Transaction transaction = Transaction.builder()
                                .profileId(profileId)
                                .amount(BigDecimal.ZERO)
                                .category(Category.builder().id(categoryId).build())
                                .description("Test")
                                .build();

                        when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId))
                                .thenReturn(Optional.of(createCategory(categoryId, profileId, CategoryType.EXPENSE)));

                        DomainException ex = assertThrows(DomainException.class,
                                () -> transactionServiceAdapter.create(transaction));

                        assertEquals("O valor da transação deve ser maior que zero.", ex.getMessage());
                }

                @Test
                @DisplayName("Should throw exception when creating installment with Bank Account")
                void shouldThrowExceptionWhenInstallmentWithBankAccount() {
                        UUID profileId = UUID.randomUUID();
                        UUID categoryId = UUID.randomUUID();

                        Transaction transaction = Transaction.builder()
                                .profileId(profileId)
                                .description("Test")
                                .amount(BigDecimal.TEN)
                                .category(Category.builder().id(categoryId).build())
                                .bankAccountId(UUID.randomUUID())
                                .isInstallment(true)
                                .totalInstallments(3)
                                .build();

                        when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId))
                                .thenReturn(Optional.of(createCategory(categoryId, profileId, CategoryType.EXPENSE)));

                        DomainException ex = assertThrows(DomainException.class,
                                () -> transactionServiceAdapter.create(transaction));

                        assertEquals("Parcelamento só é permitido para transações de cartão de crédito.", ex.getMessage());
                }

                @Test
                @DisplayName("Should throw exception when Credit Card transaction is Income")
                void shouldThrowExceptionWhenCreditCardIsIncome() {
                        UUID profileId = UUID.randomUUID();
                        UUID categoryId = UUID.randomUUID();
                        Category incomeCategory = createCategory(categoryId, profileId, CategoryType.INCOME);

                        Transaction transaction = Transaction.builder()
                                .profileId(profileId)
                                .description("Test")
                                .amount(BigDecimal.TEN)
                                .category(Category.builder().id(categoryId).build())
                                .creditCardId(UUID.randomUUID())
                                .build();

                        when(categoryRepositoryPort.findByIdAndProfileId(categoryId, profileId))
                                .thenReturn(Optional.of(incomeCategory));

                        DomainException ex = assertThrows(DomainException.class,
                                () -> transactionServiceAdapter.create(transaction));

                        assertEquals("Transações de cartão de crédito devem ser do tipo despesa.", ex.getMessage());
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
                        UUID categoryId = UUID.randomUUID();

                        Transaction existing = Transaction.builder()
                                .id(transactionId)
                                .profileId(profileId)
                                .amount(BigDecimal.valueOf(100))
                                .description("Old")
                                .category(createCategory(categoryId, profileId, CategoryType.EXPENSE))
                                .bankAccountId(UUID.randomUUID())
                                .isInstallment(false)
                                .type(TransactionType.EXPENSE)
                                .build();

                        Transaction newData = Transaction.builder()
                                .id(transactionId)
                                .description("New Desc")
                                .amount(BigDecimal.valueOf(150))
                                .build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(existing));

                        when(transactionRepositoryPort.update(any(Transaction.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                        Transaction result = transactionServiceAdapter.update(newData, profileId);

                        assertEquals("New Desc", result.getDescription());
                        assertEquals(BigDecimal.valueOf(150), result.getAmount());
                        verify(transactionRepositoryPort).update(any(Transaction.class));
                }

                @Test
                @DisplayName("Should throw exception when updating installment transaction")
                void shouldThrowExceptionWhenUpdatingInstallment() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();

                        Transaction existing = Transaction.builder()
                                .id(transactionId)
                                .profileId(profileId)
                                .isInstallment(true)
                                .build();

                        Transaction newData = Transaction.builder().id(transactionId).build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(existing));

                        assertThrows(DomainException.class,
                                () -> transactionServiceAdapter.update(newData, profileId));
                }

                @Test
                @DisplayName("Should update type if category changes")
                void shouldUpdateTypeIfCategoryChanges() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        UUID oldCatId = UUID.randomUUID();
                        UUID newCatId = UUID.randomUUID();

                        Transaction existing = Transaction.builder()
                                .id(transactionId)
                                .profileId(profileId)
                                .description("Existing")
                                .amount(BigDecimal.TEN)
                                .bankAccountId(UUID.randomUUID())
                                .category(createCategory(oldCatId, profileId, CategoryType.EXPENSE))
                                .type(TransactionType.EXPENSE)
                                .build();

                        Transaction updateRequest = Transaction.builder()
                                .id(transactionId)
                                .category(Category.builder().id(newCatId).build())
                                .build();

                        Category newCategory = createCategory(newCatId, profileId, CategoryType.INCOME);

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(existing));
                        when(categoryRepositoryPort.findByIdAndProfileId(newCatId, profileId))
                                .thenReturn(Optional.of(newCategory));
                        when(transactionRepositoryPort.update(any())).thenAnswer(inv -> inv.getArgument(0));

                        Transaction result = transactionServiceAdapter.update(updateRequest, profileId);

                        assertEquals(TransactionType.INCOME, result.getType());
                        assertEquals("Test Category", result.getCategory().getName());
                }
        }

        @Nested
        @DisplayName("Delete Transaction Tests")
        class DeleteTransactionTests {

                @Test
                @DisplayName("Should delete single transaction")
                void shouldDeleteSingleTransaction() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        Transaction transaction = Transaction.builder()
                                .id(transactionId)
                                .isInstallment(false)
                                .build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(transaction));

                        transactionServiceAdapter.delete(transactionId, profileId);

                        verify(transactionRepositoryPort).delete(transactionId);
                }

                @Test
                @DisplayName("Should delete purchase group if installment")
                void shouldDeletePurchaseGroup() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        UUID groupUuid = UUID.randomUUID();

                        Transaction transaction = Transaction.builder()
                                .id(transactionId)
                                .isInstallment(true)
                                .purchaseGroupUuid(groupUuid)
                                .build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(transaction));

                        when(transactionRepositoryPort.findByPurchaseGroupUuid(groupUuid))
                                .thenReturn(Collections.emptyList());

                        transactionServiceAdapter.delete(transactionId, profileId);

                        verify(transactionRepositoryPort).deleteByPurchaseGroupUuid(groupUuid);
                        verify(transactionRepositoryPort, never()).delete(any());
                }

                @Test
                @DisplayName("Should update invoice if deleted transaction belonged to one")
                void shouldUpdateInvoiceOnDelete() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        UUID invoiceId = UUID.randomUUID();

                        Transaction transaction = Transaction.builder()
                                .id(transactionId)
                                .isInstallment(false)
                                .invoiceId(invoiceId)
                                .build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(transaction));

                        transactionServiceAdapter.delete(transactionId, profileId);

                        verify(invoiceServicePort).updateInvoiceTotals(invoiceId);
                }
        }

        @Nested
        @DisplayName("Receipt Tests")
        class ReceiptTests {

                @Test
                @DisplayName("Should upload receipt successfully")
                void shouldUploadReceipt() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        Transaction t = Transaction.builder().id(transactionId).build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(t));
                        when(transactionRepositoryPort.update(any())).thenAnswer(inv -> inv.getArgument(0));

                        ByteArrayInputStream content = new ByteArrayInputStream(new byte[0]);
                        Transaction result = transactionServiceAdapter.uploadReceipt(
                                transactionId, profileId, content, "image/png", 100L);

                        assertNotNull(result.getAttachmentKey());
                        verify(storagePort).upload(anyString(), any(), eq("image/png"), eq(100L), any());
                }

                @Test
                @DisplayName("Should throw exception for invalid content type")
                void shouldThrowForInvalidContentType() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        ByteArrayInputStream content = new ByteArrayInputStream(new byte[0]);

                        assertThrows(DomainException.class, () ->
                                transactionServiceAdapter.uploadReceipt(transactionId, profileId, content, "text/plain", 10L));
                }

                @Test
                @DisplayName("Should delete old receipt on upload")
                void shouldDeleteOldReceiptOnUpload() {
                        UUID profileId = UUID.randomUUID();
                        UUID transactionId = UUID.randomUUID();
                        String oldKey = "old_key";

                        Transaction t = Transaction.builder().id(transactionId).attachmentKey(oldKey).build();

                        when(transactionRepositoryPort.findByIdAndProfileId(transactionId, profileId))
                                .thenReturn(Optional.of(t));
                        when(transactionRepositoryPort.update(any())).thenAnswer(inv -> inv.getArgument(0));

                        transactionServiceAdapter.uploadReceipt(
                                transactionId, profileId, new ByteArrayInputStream(new byte[0]), "image/jpeg", 10L);

                        verify(storagePort).delete(oldKey);
                }
        }

        private Category createCategory(UUID id, UUID profileId, CategoryType type) {
                return Category.builder()
                        .id(id)
                        .profileId(profileId)
                        .name("Test Category")
                        .type(type)
                        .build();
        }

        private Transaction createBankAccountTransaction(UUID profileId, UUID bankAccountId, UUID categoryId) {
                return Transaction.builder()
                        .profileId(profileId)
                        .description("Test")
                        .amount(BigDecimal.valueOf(100))
                        .transactionDate(LocalDate.now())
                        .bankAccountId(bankAccountId)
                        .category(Category.builder().id(categoryId).build())
                        .build();
        }

        private Transaction createCreditCardTransaction(UUID profileId, UUID creditCardId, UUID categoryId) {
                return Transaction.builder()
                        .profileId(profileId)
                        .description("Test CC")
                        .amount(BigDecimal.valueOf(100))
                        .transactionDate(LocalDate.now())
                        .creditCardId(creditCardId)
                        .category(Category.builder().id(categoryId).build())
                        .build();
        }

        private Transaction createInstallmentTransaction(UUID profileId, UUID creditCardId, UUID categoryId, int installments) {
                return Transaction.builder()
                        .profileId(profileId)
                        .description("Installment Purchase")
                        .amount(BigDecimal.valueOf(300))
                        .transactionDate(LocalDate.now())
                        .creditCardId(creditCardId)
                        .category(Category.builder().id(categoryId).build())
                        .isInstallment(true)
                        .totalInstallments(installments)
                        .build();
        }

        private CreditCard createCreditCard(UUID id, UUID profileId) {
                return CreditCard.builder().id(id).profileId(profileId).build();
        }

        private Invoice createInvoice(UUID id, UUID creditCardId) {
                return Invoice.builder().id(id).creditCardId(creditCardId).build();
        }
}