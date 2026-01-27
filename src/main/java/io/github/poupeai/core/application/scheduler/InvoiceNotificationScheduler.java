package io.github.poupeai.core.application.scheduler;

import io.github.poupeai.core.domain.event.InvoiceDueSoonPayload;
import io.github.poupeai.core.domain.event.InvoiceOverduePayload;
import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.model.InvoiceNotificationData;
import io.github.poupeai.core.domain.port.messaging.InvoiceNotificationProducerPort;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceNotificationScheduler {
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final InvoiceNotificationProducerPort invoiceNotificationProducerPort;

    @Value("${app.scheduler.invoice.due-soon-days}")
    private int dueSoonDays;

    @Scheduled(cron = "${app.scheduler.invoice.due-soon-cron}")
    @Transactional
    public void processInvoicesDueSoon() {
        log.info("Iniciando task de notificação de faturas próximas do vencimento");
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(dueSoonDays);
        
        var invoices = invoiceRepositoryPort.findDueSoonNotificationsData(today, endDate);
        log.info("Encontradas {} faturas próximas do vencimento para notificar", invoices.size());
        
        for (InvoiceNotificationData invoice : invoices) {
            try {
                publishDueSoonEvent(invoice);
                markDueSoonNotificationSent(invoice.getInvoiceId());
                log.debug("Notificação INVOICE_DUE_SOON enviada para fatura {}", invoice.getInvoiceId());
            } catch (Exception e) {
                log.error("Erro ao processar notificação de fatura próxima do vencimento: {}", invoice.getInvoiceId(), e);
            }
        }
        
        log.info("Task de notificação de faturas próximas do vencimento finalizada");
    }

    @Scheduled(cron = "${app.scheduler.invoice.overdue-cron}")
    @Transactional
    public void processInvoicesOverdue() {
        log.info("Iniciando task de notificação de faturas vencidas");
        
        LocalDate today = LocalDate.now();
        
        var invoices = invoiceRepositoryPort.findOverdueNotificationsData(today);
        log.info("Encontradas {} faturas vencidas para notificar", invoices.size());
        
        for (InvoiceNotificationData invoice : invoices) {
            try {
                publishOverdueEvent(invoice, today);
                markOverdueNotificationSent(invoice.getInvoiceId());
                log.debug("Notificação INVOICE_OVERDUE enviada para fatura {}", invoice.getInvoiceId());
            } catch (Exception e) {
                log.error("Erro ao processar notificação de fatura vencida: {}", invoice.getInvoiceId(), e);
            }
        }
        
        log.info("Task de notificação de faturas vencidas finalizada");
    }

    private void publishDueSoonEvent(InvoiceNotificationData invoice) {
        InvoiceDueSoonPayload payload = InvoiceDueSoonPayload.builder()
                .creditCard(invoice.getCreditCardName())
                .month(invoice.getMonth())
                .year(invoice.getYear())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmountDue())
                .invoiceDeepLink(buildInvoiceDeepLink(invoice.getInvoiceId()))
                .build();

        PoupeAiEvent<InvoiceDueSoonPayload> event = PoupeAiEvent.<InvoiceDueSoonPayload>builder()
                .messageId(UUID.randomUUID())
                .timestamp(OffsetDateTime.now())
                .triggerType("SYSTEM_SCHEDULED")
                .eventType("INVOICE_DUE_SOON")
                .recipient(PoupeAiEvent.Recipient.builder()
                        .userId(invoice.getUserId().toString())
                        .email(invoice.getUserEmail())
                        .name(invoice.getUserName())
                        .build())
                .payload(payload)
                .build();

        invoiceNotificationProducerPort.publishDueSoon(event);
    }

    private void publishOverdueEvent(InvoiceNotificationData invoice, LocalDate today) {
        int daysOverdue = (int) ChronoUnit.DAYS.between(invoice.getDueDate(), today);

        InvoiceOverduePayload payload = InvoiceOverduePayload.builder()
                .creditCard(invoice.getCreditCardName())
                .month(invoice.getMonth())
                .year(invoice.getYear())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmountDue())
                .daysOverdue(daysOverdue)
                .invoiceDeepLink(buildInvoiceDeepLink(invoice.getInvoiceId()))
                .build();

        PoupeAiEvent<InvoiceOverduePayload> event = PoupeAiEvent.<InvoiceOverduePayload>builder()
                .messageId(UUID.randomUUID())
                .timestamp(OffsetDateTime.now())
                .triggerType("SYSTEM_SCHEDULED")
                .eventType("INVOICE_OVERDUE")
                .recipient(PoupeAiEvent.Recipient.builder()
                        .userId(invoice.getUserId().toString())
                        .email(invoice.getUserEmail())
                        .name(invoice.getUserName())
                        .build())
                .payload(payload)
                .build();

        invoiceNotificationProducerPort.publishOverdue(event);
    }

    private void markDueSoonNotificationSent(UUID invoiceId) {
        invoiceRepositoryPort.findById(invoiceId).ifPresent(invoice -> {
            invoice.setDueSoonNotificationSent(true);
            invoiceRepositoryPort.update(invoice);
        });
    }

    private void markOverdueNotificationSent(UUID invoiceId) {
        invoiceRepositoryPort.findById(invoiceId).ifPresent(invoice -> {
            invoice.setOverdueNotificationSent(true);
            invoiceRepositoryPort.update(invoice);
        });
    }

    private String buildInvoiceDeepLink(UUID invoiceId) {
        return String.format("poupeai://app/invoices/%s", invoiceId);
    }
}
