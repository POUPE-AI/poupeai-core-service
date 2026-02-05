package io.github.poupeai.core.application.scheduler;

import io.github.poupeai.core.audit.Log;
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
import java.util.HashMap;
import java.util.Map;
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
        Map<String, String> jobContext = getJobContext("INVOICE_DUE_SOON_JOB");

        Log.run(getJobContext("INVOICE_DUE_SOON_JOB"), () -> {
            try {
                LocalDate today = LocalDate.now();
                LocalDate endDate = today.plusDays(dueSoonDays);
                var invoices = invoiceRepositoryPort.findDueSoonNotificationsData(today, endDate);

                if (!invoices.isEmpty()) {
                    log.info("Job iniciado: Processando {} faturas próximas do vencimento.", invoices.size());
                }

                for (InvoiceNotificationData invoice : invoices) {
                    processInvoice(invoice, "INVOICE_DUE_SOON", () -> {
                        publishDueSoonEvent(invoice);
                        markDueSoonNotificationSent(invoice.getInvoiceId());
                    });
                }
            } catch (Exception e) {
                log.error("Falha crítica na execução do Job INVOICE_DUE_SOON", e);
            }
        });
    }

    @Scheduled(cron = "${app.scheduler.invoice.overdue-cron}")
    @Transactional
    public void processInvoicesOverdue() {
        Log.run(getJobContext("INVOICE_OVERDUE_JOB"), () -> {
            try {
                LocalDate today = LocalDate.now();
                var invoices = invoiceRepositoryPort.findOverdueNotificationsData(today);

                if (!invoices.isEmpty()) {
                    log.info("Job iniciado: Processando {} faturas vencidas.", invoices.size());
                }

                for (InvoiceNotificationData invoice : invoices) {
                    processInvoice(invoice, "INVOICE_OVERDUE", () -> {
                        publishOverdueEvent(invoice, today);
                        markOverdueNotificationSent(invoice.getInvoiceId());
                    });
                }
            } catch (Exception e) {
                log.error("Falha crítica na execução do Job INVOICE_OVERDUE", e);
            }
        });
    }

    private Map<String, String> getJobContext(String jobName) {
        return Map.of(
                "trace.correlation_id", UUID.randomUUID().toString(),
                "context.trigger_type", "system_scheduled",
                "context.job_name", jobName
        );
    }

    private void processInvoice(InvoiceNotificationData invoice, String eventType, Runnable action) {
        Map<String, String> userContext = new HashMap<>();
        userContext.put("user.id", invoice.getUserId().toString());
        userContext.put("user.email", invoice.getUserEmail());

        Log.run(userContext, () -> {
            try {
                action.run();
                Log.event(log, eventType, "Notificação enviada com sucesso para fatura {}", invoice.getInvoiceId());
            } catch (Exception e) {
                userContext.put("context.invoice_id", invoice.getInvoiceId().toString());
                Log.run(userContext, () -> Log.error(log, eventType + "_FAIL", "Erro ao processar notificação de fatura", e));
            }
        });
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
