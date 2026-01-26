package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceAdapterTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @InjectMocks
    private InvoiceServiceAdapter invoiceServiceAdapter;

    @Nested
    @DisplayName("Get Or Create Invoice Tests")
    class GetOrCreateInvoiceTests {

        @Test
        @DisplayName("Should return existing invoice when found")
        void shouldReturnExistingInvoiceWhenFound() {
            UUID creditCardId = UUID.randomUUID();
            CreditCard creditCard = createCreditCard(creditCardId, 10, 20);
            LocalDate transactionDate = LocalDate.of(2024, 1, 5);
            Invoice existingInvoice = createInvoice(creditCardId, 1, 2024);

            when(invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(creditCardId, 1, 2024))
                    .thenReturn(Optional.of(existingInvoice));

            Invoice result = invoiceServiceAdapter.getOrCreateInvoiceForDate(creditCard, transactionDate);

            assertNotNull(result);
            assertEquals(existingInvoice.getId(), result.getId());
            verify(invoiceRepositoryPort, never()).create(any());
        }

        @Test
        @DisplayName("Should create new invoice when not found")
        void shouldCreateNewInvoiceWhenNotFound() {
            UUID creditCardId = UUID.randomUUID();
            CreditCard creditCard = createCreditCard(creditCardId, 10, 20);
            LocalDate transactionDate = LocalDate.of(2024, 1, 5);

            when(invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(creditCardId, 1, 2024))
                    .thenReturn(Optional.empty());
            when(invoiceRepositoryPort.create(any())).thenAnswer(inv -> {
                Invoice invoice = inv.getArgument(0);
                invoice.setId(UUID.randomUUID());
                return invoice;
            });

            Invoice result = invoiceServiceAdapter.getOrCreateInvoiceForDate(creditCard, transactionDate);

            assertNotNull(result);
            verify(invoiceRepositoryPort).create(any());
            
            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepositoryPort).create(captor.capture());
            Invoice createdInvoice = captor.getValue();
            
            assertEquals(1, createdInvoice.getMonth());
            assertEquals(2024, createdInvoice.getYear());
            assertEquals(InvoiceStatus.OPEN, createdInvoice.getStatus());
        }

        @Test
        @DisplayName("Should allocate to next month invoice when purchase after closing day")
        void shouldAllocateToNextMonthWhenAfterClosingDay() {
            UUID creditCardId = UUID.randomUUID();
            CreditCard creditCard = createCreditCard(creditCardId, 10, 20);
            LocalDate transactionDate = LocalDate.of(2024, 1, 15);

            when(invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(creditCardId, 2, 2024))
                    .thenReturn(Optional.empty());
            when(invoiceRepositoryPort.create(any())).thenAnswer(inv -> {
                Invoice invoice = inv.getArgument(0);
                invoice.setId(UUID.randomUUID());
                return invoice;
            });

            invoiceServiceAdapter.getOrCreateInvoiceForDate(creditCard, transactionDate);

            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepositoryPort).create(captor.capture());
            Invoice createdInvoice = captor.getValue();
            
            assertEquals(2, createdInvoice.getMonth());
            assertEquals(2024, createdInvoice.getYear());
        }

        @Test
        @DisplayName("Should handle year transition correctly")
        void shouldHandleYearTransitionCorrectly() {
            UUID creditCardId = UUID.randomUUID();
            CreditCard creditCard = createCreditCard(creditCardId, 10, 20);
            LocalDate transactionDate = LocalDate.of(2024, 12, 15);

            when(invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(creditCardId, 1, 2025))
                    .thenReturn(Optional.empty());
            when(invoiceRepositoryPort.create(any())).thenAnswer(inv -> {
                Invoice invoice = inv.getArgument(0);
                invoice.setId(UUID.randomUUID());
                return invoice;
            });

            invoiceServiceAdapter.getOrCreateInvoiceForDate(creditCard, transactionDate);

            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepositoryPort).create(captor.capture());
            Invoice createdInvoice = captor.getValue();
            
            assertEquals(1, createdInvoice.getMonth());
            assertEquals(2025, createdInvoice.getYear());
        }

        @Test
        @DisplayName("Should allocate to current month invoice when purchase on closing day")
        void shouldAllocateToCurrentMonthWhenOnClosingDay() {
            UUID creditCardId = UUID.randomUUID();
            CreditCard creditCard = createCreditCard(creditCardId, 10, 20);
            LocalDate transactionDate = LocalDate.of(2024, 1, 10);

            when(invoiceRepositoryPort.findByCreditCardIdAndMonthAndYear(creditCardId, 1, 2024))
                    .thenReturn(Optional.empty());
            when(invoiceRepositoryPort.create(any())).thenAnswer(inv -> {
                Invoice invoice = inv.getArgument(0);
                invoice.setId(UUID.randomUUID());
                return invoice;
            });

            invoiceServiceAdapter.getOrCreateInvoiceForDate(creditCard, transactionDate);

            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepositoryPort).create(captor.capture());
            Invoice createdInvoice = captor.getValue();
            
            assertEquals(1, createdInvoice.getMonth());
            assertEquals(2024, createdInvoice.getYear());
        }
    }

    @Nested
    @DisplayName("Find Invoice Tests")
    class FindInvoiceTests {

        @Test
        @DisplayName("Should find invoice by ID successfully")
        void shouldFindInvoiceByIdSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024);
            invoice.setId(invoiceId);

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.of(invoice));

            Invoice result = invoiceServiceAdapter.findById(invoiceId, profileId);

            assertNotNull(result);
            assertEquals(invoiceId, result.getId());
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void shouldThrowExceptionWhenInvoiceNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> invoiceServiceAdapter.findById(invoiceId, profileId));
        }

        @Test
        @DisplayName("Should find invoices by credit card ID")
        void shouldFindInvoicesByCreditCardId() {
            UUID profileId = UUID.randomUUID();
            UUID creditCardId = UUID.randomUUID();
            List<Invoice> invoices = List.of(
                    createInvoice(creditCardId, 1, 2024),
                    createInvoice(creditCardId, 2, 2024)
            );

            when(invoiceRepositoryPort.findByCreditCardId(creditCardId)).thenReturn(invoices);

            List<Invoice> result = invoiceServiceAdapter.findByCreditCardId(creditCardId, profileId);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should find invoices by profile ID")
        void shouldFindInvoicesByProfileId() {
            UUID profileId = UUID.randomUUID();
            List<Invoice> invoices = List.of(
                    createInvoice(UUID.randomUUID(), 1, 2024),
                    createInvoice(UUID.randomUUID(), 2, 2024)
            );

            when(invoiceRepositoryPort.findByProfileId(profileId)).thenReturn(invoices);

            List<Invoice> result = invoiceServiceAdapter.findByProfileId(profileId);

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Update Invoice Totals Tests")
    class UpdateInvoiceTotalsTests {

        @Test
        @DisplayName("Should update invoice totals successfully")
        void shouldUpdateInvoiceTotalsSuccessfully() {
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024);
            invoice.setId(invoiceId);
            invoice.setTotalAmount(BigDecimal.ZERO);

            when(invoiceRepositoryPort.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(transactionRepositoryPort.sumAmountByInvoiceId(invoiceId))
                    .thenReturn(BigDecimal.valueOf(500));
            when(invoiceRepositoryPort.update(any())).thenAnswer(inv -> inv.getArgument(0));

            invoiceServiceAdapter.updateInvoiceTotals(invoiceId);

            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepositoryPort).update(captor.capture());
            Invoice updatedInvoice = captor.getValue();
            
            assertEquals(BigDecimal.valueOf(500), updatedInvoice.getTotalAmount());
        }

        @Test
        @DisplayName("Should throw exception when invoice not found for update")
        void shouldThrowExceptionWhenInvoiceNotFoundForUpdate() {
            UUID invoiceId = UUID.randomUUID();

            when(invoiceRepositoryPort.findById(invoiceId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> invoiceServiceAdapter.updateInvoiceTotals(invoiceId));
        }
    }

    @Nested
    @DisplayName("Delete Invoice Tests")
    class DeleteInvoiceTests {

        @Test
        @DisplayName("Should delete invoice and associated transactions successfully")
        void shouldDeleteInvoiceSuccessfully() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024, InvoiceStatus.OPEN);
            invoice.setId(invoiceId);

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.of(invoice));

            invoiceServiceAdapter.deleteInvoice(invoiceId, profileId);

            verify(transactionRepositoryPort).deleteByInvoiceId(invoiceId);
            verify(invoiceRepositoryPort).delete(invoiceId);
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void shouldThrowExceptionWhenInvoiceNotFound() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> invoiceServiceAdapter.deleteInvoice(invoiceId, profileId));
        }

        @Test
        @DisplayName("Should throw exception when trying to delete paid invoice")
        void shouldThrowExceptionWhenDeletingPaidInvoice() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024, InvoiceStatus.PAID);
            invoice.setId(invoiceId);

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.of(invoice));

            DomainException exception = assertThrows(DomainException.class, 
                    () -> invoiceServiceAdapter.deleteInvoice(invoiceId, profileId));
            assertEquals("Não é possível deletar uma fatura com pagamentos registrados. Estorne os pagamentos primeiro.", 
                    exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when trying to delete partially paid invoice")
        void shouldThrowExceptionWhenDeletingPartiallyPaidInvoice() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024, InvoiceStatus.PARTIALLY_PAID);
            invoice.setId(invoiceId);

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.of(invoice));

            DomainException exception = assertThrows(DomainException.class, 
                    () -> invoiceServiceAdapter.deleteInvoice(invoiceId, profileId));
            assertEquals("Não é possível deletar uma fatura com pagamentos registrados. Estorne os pagamentos primeiro.", 
                    exception.getMessage());
        }

        @Test
        @DisplayName("Should allow deleting closed invoice without payments")
        void shouldAllowDeletingClosedInvoice() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024, InvoiceStatus.CLOSED);
            invoice.setId(invoiceId);

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.of(invoice));

            invoiceServiceAdapter.deleteInvoice(invoiceId, profileId);

            verify(transactionRepositoryPort).deleteByInvoiceId(invoiceId);
            verify(invoiceRepositoryPort).delete(invoiceId);
        }

        @Test
        @DisplayName("Should allow deleting overdue invoice without payments")
        void shouldAllowDeletingOverdueInvoice() {
            UUID profileId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Invoice invoice = createInvoice(UUID.randomUUID(), 1, 2024, InvoiceStatus.OVERDUE);
            invoice.setId(invoiceId);

            when(invoiceRepositoryPort.findByIdAndProfileId(invoiceId, profileId))
                    .thenReturn(Optional.of(invoice));

            invoiceServiceAdapter.deleteInvoice(invoiceId, profileId);

            verify(transactionRepositoryPort).deleteByInvoiceId(invoiceId);
            verify(invoiceRepositoryPort).delete(invoiceId);
        }
    }

    private CreditCard createCreditCard(UUID id, int closingDay, int dueDay) {
        return CreditCard.builder()
                .id(id)
                .profileId(UUID.randomUUID())
                .name("Test Card")
                .closingDay(closingDay)
                .dueDay(dueDay)
                .creditLimit(BigDecimal.valueOf(5000))
                .build();
    }

    private Invoice createInvoice(UUID creditCardId, int month, int year) {
        return createInvoice(creditCardId, month, year, InvoiceStatus.OPEN);
    }

    private Invoice createInvoice(UUID creditCardId, int month, int year, InvoiceStatus status) {
        return Invoice.builder()
                .id(UUID.randomUUID())
                .creditCardId(creditCardId)
                .month(month)
                .year(year)
                .closingDate(LocalDate.of(year, month, 10))
                .dueDate(LocalDate.of(year, month, 20).plusMonths(1))
                .totalAmount(BigDecimal.valueOf(1000))
                .paidAmount(status == InvoiceStatus.PAID ? BigDecimal.valueOf(1000) : BigDecimal.ZERO)
                .status(status)
                .dueSoonNotificationSent(false)
                .overdueNotificationSent(false)
                .build();
    }
}
