package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.persistence.entity.CreditCardEntity;
import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import io.github.poupeai.core.persistence.mapper.InvoiceEntityMapper;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
import org.junit.jupiter.api.DisplayName;
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
class InvoiceRepositoryAdapterTest {

    @InjectMocks
    private InvoiceRepositoryAdapter adapter;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceEntityMapper invoiceMapper;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Test
    @DisplayName("Should create invoice successfully")
    void shouldCreateInvoiceSuccessfully() {
        UUID creditCardId = UUID.randomUUID();
        Invoice domain = Invoice.builder()
                .creditCardId(creditCardId)
                .month(1)
                .year(2026)
                .closingDate(LocalDate.of(2026, 1, 10))
                .dueDate(LocalDate.of(2026, 2, 10))
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .build();

        InvoiceEntity entity = new InvoiceEntity();
        CreditCardEntity creditCardProxy = new CreditCardEntity();

        when(invoiceMapper.toEntity(domain)).thenReturn(entity);
        when(creditCardRepository.getReferenceById(creditCardId)).thenReturn(creditCardProxy);
        when(invoiceRepository.save(entity)).thenReturn(entity);
        when(invoiceMapper.toDomain(entity)).thenReturn(domain);

        Invoice result = adapter.create(domain);

        assertNotNull(result);
        verify(invoiceRepository).save(entity);
        assertEquals(creditCardProxy, entity.getCreditCard());
    }

    @Test
    @DisplayName("Should update invoice successfully")
    void shouldUpdateInvoiceSuccessfully() {
        UUID invoiceId = UUID.randomUUID();
        Invoice domain = Invoice.builder()
                .id(invoiceId)
                .totalAmount(BigDecimal.valueOf(1000))
                .paidAmount(BigDecimal.valueOf(500))
                .status(InvoiceStatus.PARTIALLY_PAID)
                .dueSoonNotificationSent(true)
                .overdueNotificationSent(false)
                .build();

        InvoiceEntity existingEntity = new InvoiceEntity();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingEntity));
        when(invoiceRepository.save(existingEntity)).thenReturn(existingEntity);
        when(invoiceMapper.toDomain(existingEntity)).thenReturn(domain);

        Invoice result = adapter.update(domain);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000), existingEntity.getTotalAmount());
        assertEquals(BigDecimal.valueOf(500), existingEntity.getPaidAmount());
        assertEquals(InvoiceStatus.PARTIALLY_PAID, existingEntity.getStatus());
        assertTrue(existingEntity.getDueSoonNotificationSent());
        assertFalse(existingEntity.getOverdueNotificationSent());
        verify(invoiceRepository).save(existingEntity);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing invoice")
    void shouldThrowExceptionWhenUpdatingNonExistingInvoice() {
        UUID invoiceId = UUID.randomUUID();
        Invoice domain = Invoice.builder().id(invoiceId).build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adapter.update(domain));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find invoice by id")
    void shouldFindInvoiceById() {
        UUID id = UUID.randomUUID();
        InvoiceEntity entity = new InvoiceEntity();
        Invoice domain = new Invoice();

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(entity));
        when(invoiceMapper.toDomain(entity)).thenReturn(domain);

        Optional<Invoice> result = adapter.findById(id);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when invoice not found by id")
    void shouldReturnEmptyWhenInvoiceNotFoundById() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Invoice> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find invoice by id and profile id")
    void shouldFindInvoiceByIdAndProfileId() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        InvoiceEntity entity = new InvoiceEntity();
        Invoice domain = new Invoice();

        when(invoiceRepository.findByIdAndCreditCardProfileUserId(id, profileId))
                .thenReturn(Optional.of(entity));
        when(invoiceMapper.toDomain(entity)).thenReturn(domain);

        Optional<Invoice> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should find invoice by credit card id, month and year")
    void shouldFindInvoiceByCreditCardIdAndMonthAndYear() {
        UUID creditCardId = UUID.randomUUID();
        InvoiceEntity entity = new InvoiceEntity();
        Invoice domain = new Invoice();

        when(invoiceRepository.findByCreditCardIdAndMonthAndYear(creditCardId, 1, 2026))
                .thenReturn(Optional.of(entity));
        when(invoiceMapper.toDomain(entity)).thenReturn(domain);

        Optional<Invoice> result = adapter.findByCreditCardIdAndMonthAndYear(creditCardId, 1, 2026);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should find invoices by credit card id")
    void shouldFindInvoicesByCreditCardId() {
        UUID creditCardId = UUID.randomUUID();
        List<InvoiceEntity> entities = List.of(new InvoiceEntity());
        List<Invoice> domains = List.of(new Invoice());

        when(invoiceRepository.findByCreditCardId(creditCardId)).thenReturn(entities);
        when(invoiceMapper.toDomainList(entities)).thenReturn(domains);

        List<Invoice> result = adapter.findByCreditCardId(creditCardId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should find invoices by profile id")
    void shouldFindInvoicesByProfileId() {
        UUID profileId = UUID.randomUUID();
        List<InvoiceEntity> entities = List.of(new InvoiceEntity());
        List<Invoice> domains = List.of(new Invoice());

        when(invoiceRepository.findByCreditCardProfileUserId(profileId)).thenReturn(entities);
        when(invoiceMapper.toDomainList(entities)).thenReturn(domains);

        List<Invoice> result = adapter.findByProfileId(profileId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should delete invoice by id")
    void shouldDeleteInvoiceById() {
        UUID id = UUID.randomUUID();

        adapter.delete(id);

        verify(invoiceRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should check if invoice exists by credit card, month and year")
    void shouldCheckIfInvoiceExists() {
        UUID creditCardId = UUID.randomUUID();

        when(invoiceRepository.existsByCreditCardIdAndMonthAndYear(creditCardId, 1, 2026))
                .thenReturn(true);

        assertTrue(adapter.existsByCreditCardIdAndMonthAndYear(creditCardId, 1, 2026));
    }
}
