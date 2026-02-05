package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import io.github.poupeai.core.persistence.entity.InvoicePaymentEntity;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import io.github.poupeai.core.persistence.mapper.InvoicePaymentEntityMapper;
import io.github.poupeai.core.persistence.repository.InvoicePaymentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoicePaymentRepositoryAdapterTest {

    @InjectMocks
    private InvoicePaymentRepositoryAdapter adapter;

    @Mock
    private InvoicePaymentRepository invoicePaymentRepository;

    @Mock
    private InvoicePaymentEntityMapper invoicePaymentEntityMapper;

    @Mock
    private EntityManager entityManager;

    @Test
    @DisplayName("Should create invoice payment successfully")
    void shouldCreateInvoicePaymentSuccessfully() {
        UUID invoiceId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Long paymentId = 1L;

        InvoicePayment domain = InvoicePayment.builder()
                .invoiceId(invoiceId)
                .paymentTransactionId(transactionId)
                .amount(BigDecimal.valueOf(500))
                .build();

        InvoicePaymentEntity entity = new InvoicePaymentEntity();
        InvoiceEntity invoiceProxy = new InvoiceEntity();
        TransactionEntity transactionProxy = new TransactionEntity();

        InvoicePayment savedDomain = InvoicePayment.builder()
                .id(paymentId)
                .invoiceId(invoiceId)
                .paymentTransactionId(transactionId)
                .amount(BigDecimal.valueOf(500))
                .build();

        when(invoicePaymentEntityMapper.toEntity(domain)).thenReturn(entity);
        when(entityManager.getReference(InvoiceEntity.class, invoiceId)).thenReturn(invoiceProxy);
        when(entityManager.getReference(TransactionEntity.class, transactionId)).thenReturn(transactionProxy);
        when(invoicePaymentRepository.save(entity)).thenReturn(entity);
        when(invoicePaymentEntityMapper.toDomain(entity)).thenReturn(savedDomain);

        InvoicePayment result = adapter.create(domain);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertEquals(invoiceId, result.getInvoiceId());
        assertEquals(transactionId, result.getPaymentTransactionId());
        verify(invoicePaymentRepository).save(entity);
        assertEquals(invoiceProxy, entity.getInvoice());
        assertEquals(transactionProxy, entity.getPaymentTransaction());
    }

    @Test
    @DisplayName("Should find invoice payment by id")
    void shouldFindInvoicePaymentById() {
        Long id = 1L;
        InvoicePaymentEntity entity = new InvoicePaymentEntity();
        InvoicePayment domain = InvoicePayment.builder().id(id).build();

        when(invoicePaymentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(invoicePaymentEntityMapper.toDomain(entity)).thenReturn(domain);

        Optional<InvoicePayment> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty when payment not found by id")
    void shouldReturnEmptyWhenPaymentNotFoundById() {
        Long id = 1L;

        when(invoicePaymentRepository.findById(id)).thenReturn(Optional.empty());

        Optional<InvoicePayment> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find invoice payment by transaction id")
    void shouldFindInvoicePaymentByTransactionId() {
        UUID transactionId = UUID.randomUUID();
        InvoicePaymentEntity entity = new InvoicePaymentEntity();
        InvoicePayment domain = InvoicePayment.builder()
                .paymentTransactionId(transactionId)
                .build();

        when(invoicePaymentRepository.findByPaymentTransactionId(transactionId))
                .thenReturn(Optional.of(entity));
        when(invoicePaymentEntityMapper.toDomain(entity)).thenReturn(domain);

        Optional<InvoicePayment> result = adapter.findByPaymentTransactionId(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getPaymentTransactionId());
    }

    @Test
    @DisplayName("Should find invoice payments by invoice id")
    void shouldFindInvoicePaymentsByInvoiceId() {
        UUID invoiceId = UUID.randomUUID();
        List<InvoicePaymentEntity> entities = List.of(new InvoicePaymentEntity(), new InvoicePaymentEntity());
        List<InvoicePayment> domains = List.of(
                InvoicePayment.builder().id(1L).build(),
                InvoicePayment.builder().id(2L).build()
        );

        when(invoicePaymentRepository.findByInvoiceId(invoiceId)).thenReturn(entities);
        when(invoicePaymentEntityMapper.toDomainList(entities)).thenReturn(domains);

        List<InvoicePayment> result = adapter.findByInvoiceId(invoiceId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should delete invoice payment by id")
    void shouldDeleteInvoicePaymentById() {
        Long id = 1L;

        adapter.delete(id);

        verify(invoicePaymentRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should delete invoice payments by invoice id")
    void shouldDeleteInvoicePaymentsByInvoiceId() {
        UUID invoiceId = UUID.randomUUID();

        adapter.deleteByInvoiceId(invoiceId);

        verify(invoicePaymentRepository).deleteByInvoiceId(invoiceId);
    }
}
