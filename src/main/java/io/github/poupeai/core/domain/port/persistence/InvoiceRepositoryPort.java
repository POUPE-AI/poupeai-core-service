package io.github.poupeai.core.domain.port.persistence;

import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceFilter;
import io.github.poupeai.core.domain.model.InvoiceNotificationData;
import io.github.poupeai.core.domain.model.PageDomain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepositoryPort {
    Invoice create(Invoice invoice);

    Invoice update(Invoice invoice);

    Optional<Invoice> findById(UUID id);

    Optional<Invoice> findByIdAndProfileId(UUID id, UUID profileId);

    Optional<Invoice> findByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year);

    List<Invoice> findByCreditCardId(UUID creditCardId);

    List<Invoice> findByProfileId(UUID profileId);

    void delete(UUID id);

    boolean existsByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year);

    List<Invoice> findDueSoonNotNotified(LocalDate startDate, LocalDate endDate);

    List<Invoice> findOverdueNotNotified(LocalDate today);

    List<InvoiceNotificationData> findDueSoonNotificationsData(LocalDate startDate, LocalDate endDate);

    List<InvoiceNotificationData> findOverdueNotificationsData(LocalDate today);

    PageDomain<Invoice> search(UUID profileId, InvoiceFilter filter);

    List<Invoice> findByProfileIdAndMonthAndYear(UUID profileId, Integer month, Integer year);
}
