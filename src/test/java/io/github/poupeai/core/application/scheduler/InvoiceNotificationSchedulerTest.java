package io.github.poupeai.core.application.scheduler;

import io.github.poupeai.core.domain.event.InvoiceDueSoonPayload;
import io.github.poupeai.core.domain.event.InvoiceOverduePayload;
import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceNotificationData;
import io.github.poupeai.core.domain.port.messaging.InvoiceNotificationProducerPort;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceNotificationSchedulerTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;

    @Mock
    private InvoiceNotificationProducerPort invoiceNotificationProducerPort;

    @InjectMocks
    private InvoiceNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "dueSoonDays", 3);
    }

    @Nested
    @DisplayName("processInvoicesDueSoon")
    class ProcessInvoicesDueSoonTests {

        @Test
        @DisplayName("Should publish INVOICE_DUE_SOON event for invoices near due date")
        void shouldPublishDueSoonEventForInvoicesNearDueDate() {
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(2);
            UUID invoiceId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            InvoiceNotificationData notificationData = InvoiceNotificationData.builder()
                    .invoiceId(invoiceId)
                    .creditCardId(UUID.randomUUID())
                    .creditCardName("Cartão Nubank")
                    .month(7)
                    .year(2025)
                    .dueDate(dueDate)
                    .totalAmount(new BigDecimal("500.00"))
                    .paidAmount(BigDecimal.ZERO)
                    .userId(userId)
                    .userEmail("user@email.com")
                    .userName("João Silva")
                    .build();

            Invoice invoice = Invoice.builder()
                    .id(invoiceId)
                    .dueSoonNotificationSent(false)
                    .build();

            when(invoiceRepositoryPort.findDueSoonNotificationsData(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(notificationData));
            when(invoiceRepositoryPort.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceRepositoryPort.update(any(Invoice.class))).thenReturn(invoice);

            scheduler.processInvoicesDueSoon();

            ArgumentCaptor<PoupeAiEvent<?>> eventCaptor = ArgumentCaptor.forClass(PoupeAiEvent.class);
            verify(invoiceNotificationProducerPort).publishDueSoon(eventCaptor.capture());

            PoupeAiEvent<?> capturedEvent = eventCaptor.getValue();
            assertEquals("INVOICE_DUE_SOON", capturedEvent.getEventType());
            assertEquals("SYSTEM_SCHEDULED", capturedEvent.getTriggerType());
            assertNotNull(capturedEvent.getMessageId());
            assertNotNull(capturedEvent.getTimestamp());

            PoupeAiEvent.Recipient recipient = capturedEvent.getRecipient();
            assertEquals(userId.toString(), recipient.getUserId());
            assertEquals("user@email.com", recipient.getEmail());
            assertEquals("João Silva", recipient.getName());

            InvoiceDueSoonPayload payload = (InvoiceDueSoonPayload) capturedEvent.getPayload();
            assertEquals("Cartão Nubank", payload.getCreditCard());
            assertEquals(7, payload.getMonth());
            assertEquals(2025, payload.getYear());
            assertEquals(dueDate, payload.getDueDate());
            assertEquals(new BigDecimal("500.00"), payload.getAmount());
            assertTrue(payload.getInvoiceDeepLink().contains(invoiceId.toString()));

            verify(invoiceRepositoryPort).findById(invoiceId);
            verify(invoiceRepositoryPort).update(argThat(inv -> inv.getDueSoonNotificationSent()));
        }

        @Test
        @DisplayName("Should not publish event when no invoices are due soon")
        void shouldNotPublishEventWhenNoInvoicesAreDueSoon() {
            when(invoiceRepositoryPort.findDueSoonNotificationsData(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.processInvoicesDueSoon();

            verifyNoInteractions(invoiceNotificationProducerPort);
            verify(invoiceRepositoryPort, never()).update(any());
        }

        @Test
        @DisplayName("Should continue processing other invoices when one fails")
        void shouldContinueProcessingWhenOneFails() {
            UUID invoiceId1 = UUID.randomUUID();
            UUID invoiceId2 = UUID.randomUUID();

            InvoiceNotificationData data1 = createNotificationData(invoiceId1, "Cartão 1");
            InvoiceNotificationData data2 = createNotificationData(invoiceId2, "Cartão 2");

            Invoice invoice2 = Invoice.builder().id(invoiceId2).dueSoonNotificationSent(false).build();

            when(invoiceRepositoryPort.findDueSoonNotificationsData(any(), any()))
                    .thenReturn(List.of(data1, data2));
            
            doThrow(new RuntimeException("Publish failed")).when(invoiceNotificationProducerPort)
                    .publishDueSoon(argThat(e -> e.getPayload() instanceof InvoiceDueSoonPayload p && 
                            p.getCreditCard().equals("Cartão 1")));
            
            when(invoiceRepositoryPort.findById(invoiceId2)).thenReturn(Optional.of(invoice2));
            when(invoiceRepositoryPort.update(any())).thenReturn(invoice2);

            scheduler.processInvoicesDueSoon();

            verify(invoiceNotificationProducerPort, times(2)).publishDueSoon(any());
            verify(invoiceRepositoryPort).update(any());
        }
    }

    @Nested
    @DisplayName("processInvoicesOverdue")
    class ProcessInvoicesOverdueTests {

        @Test
        @DisplayName("Should publish INVOICE_OVERDUE event for overdue invoices")
        void shouldPublishOverdueEventForOverdueInvoices() {
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.minusDays(5);
            UUID invoiceId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            InvoiceNotificationData notificationData = InvoiceNotificationData.builder()
                    .invoiceId(invoiceId)
                    .creditCardId(UUID.randomUUID())
                    .creditCardName("Cartão BB")
                    .month(6)
                    .year(2025)
                    .dueDate(dueDate)
                    .totalAmount(new BigDecimal("1000.00"))
                    .paidAmount(new BigDecimal("200.00"))
                    .userId(userId)
                    .userEmail("maria@email.com")
                    .userName("Maria Santos")
                    .build();

            Invoice invoice = Invoice.builder()
                    .id(invoiceId)
                    .overdueNotificationSent(false)
                    .build();

            when(invoiceRepositoryPort.findOverdueNotificationsData(any(LocalDate.class)))
                    .thenReturn(List.of(notificationData));
            when(invoiceRepositoryPort.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceRepositoryPort.update(any(Invoice.class))).thenReturn(invoice);

            scheduler.processInvoicesOverdue();

            ArgumentCaptor<PoupeAiEvent<?>> eventCaptor = ArgumentCaptor.forClass(PoupeAiEvent.class);
            verify(invoiceNotificationProducerPort).publishOverdue(eventCaptor.capture());

            PoupeAiEvent<?> capturedEvent = eventCaptor.getValue();
            assertEquals("INVOICE_OVERDUE", capturedEvent.getEventType());
            assertEquals("SYSTEM_SCHEDULED", capturedEvent.getTriggerType());

            PoupeAiEvent.Recipient recipient = capturedEvent.getRecipient();
            assertEquals(userId.toString(), recipient.getUserId());
            assertEquals("maria@email.com", recipient.getEmail());
            assertEquals("Maria Santos", recipient.getName());

            InvoiceOverduePayload payload = (InvoiceOverduePayload) capturedEvent.getPayload();
            assertEquals("Cartão BB", payload.getCreditCard());
            assertEquals(6, payload.getMonth());
            assertEquals(2025, payload.getYear());
            assertEquals(dueDate, payload.getDueDate());
            assertEquals(new BigDecimal("800.00"), payload.getAmount());
            assertEquals(5, payload.getDaysOverdue());
            assertTrue(payload.getInvoiceDeepLink().contains(invoiceId.toString()));

            verify(invoiceRepositoryPort).update(argThat(inv -> inv.getOverdueNotificationSent()));
        }

        @Test
        @DisplayName("Should not publish event when no invoices are overdue")
        void shouldNotPublishEventWhenNoInvoicesAreOverdue() {
            when(invoiceRepositoryPort.findOverdueNotificationsData(any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            scheduler.processInvoicesOverdue();

            verifyNoInteractions(invoiceNotificationProducerPort);
            verify(invoiceRepositoryPort, never()).update(any());
        }

        @Test
        @DisplayName("Should calculate correct days overdue")
        void shouldCalculateCorrectDaysOverdue() {
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.minusDays(10);
            UUID invoiceId = UUID.randomUUID();

            InvoiceNotificationData notificationData = InvoiceNotificationData.builder()
                    .invoiceId(invoiceId)
                    .creditCardId(UUID.randomUUID())
                    .creditCardName("Cartão Test")
                    .month(5)
                    .year(2025)
                    .dueDate(dueDate)
                    .totalAmount(new BigDecimal("300.00"))
                    .paidAmount(BigDecimal.ZERO)
                    .userId(UUID.randomUUID())
                    .userEmail("test@email.com")
                    .userName("Test User")
                    .build();

            Invoice invoice = Invoice.builder().id(invoiceId).overdueNotificationSent(false).build();

            when(invoiceRepositoryPort.findOverdueNotificationsData(any(LocalDate.class)))
                    .thenReturn(List.of(notificationData));
            when(invoiceRepositoryPort.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceRepositoryPort.update(any())).thenReturn(invoice);

            scheduler.processInvoicesOverdue();

            ArgumentCaptor<PoupeAiEvent<?>> eventCaptor = ArgumentCaptor.forClass(PoupeAiEvent.class);
            verify(invoiceNotificationProducerPort).publishOverdue(eventCaptor.capture());

            InvoiceOverduePayload payload = (InvoiceOverduePayload) eventCaptor.getValue().getPayload();
            assertEquals(10, payload.getDaysOverdue());
        }
    }

    private InvoiceNotificationData createNotificationData(UUID invoiceId, String cardName) {
        return InvoiceNotificationData.builder()
                .invoiceId(invoiceId)
                .creditCardId(UUID.randomUUID())
                .creditCardName(cardName)
                .month(7)
                .year(2025)
                .dueDate(LocalDate.now().plusDays(2))
                .totalAmount(new BigDecimal("100.00"))
                .paidAmount(BigDecimal.ZERO)
                .userId(UUID.randomUUID())
                .userEmail("test@email.com")
                .userName("Test User")
                .build();
    }
}
