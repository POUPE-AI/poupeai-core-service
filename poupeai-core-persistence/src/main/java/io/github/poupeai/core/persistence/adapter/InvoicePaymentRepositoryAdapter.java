package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.domain.port.persistence.InvoicePaymentRepositoryPort;
import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import io.github.poupeai.core.persistence.entity.InvoicePaymentEntity;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import io.github.poupeai.core.persistence.mapper.InvoicePaymentEntityMapper;
import io.github.poupeai.core.persistence.repository.InvoicePaymentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InvoicePaymentRepositoryAdapter implements InvoicePaymentRepositoryPort {
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final InvoicePaymentEntityMapper invoicePaymentEntityMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public InvoicePayment create(InvoicePayment invoicePayment) {
        InvoicePaymentEntity entity = invoicePaymentEntityMapper.toEntity(invoicePayment);
        
        entity.setInvoice(entityManager.getReference(InvoiceEntity.class, invoicePayment.getInvoiceId()));
        entity.setPaymentTransaction(entityManager.getReference(TransactionEntity.class, invoicePayment.getPaymentTransactionId()));
        
        InvoicePaymentEntity savedEntity = invoicePaymentRepository.save(entity);
        return invoicePaymentEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<InvoicePayment> findById(Long id) {
        return invoicePaymentRepository.findById(id)
                .map(invoicePaymentEntityMapper::toDomain);
    }

    @Override
    public Optional<InvoicePayment> findByPaymentTransactionId(UUID paymentTransactionId) {
        return invoicePaymentRepository.findByPaymentTransactionId(paymentTransactionId)
                .map(invoicePaymentEntityMapper::toDomain);
    }

    @Override
    public List<InvoicePayment> findByInvoiceId(UUID invoiceId) {
        return invoicePaymentEntityMapper.toDomainList(
                invoicePaymentRepository.findByInvoiceId(invoiceId)
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        invoicePaymentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByInvoiceId(UUID invoiceId) {
        invoicePaymentRepository.deleteByInvoiceId(invoiceId);
    }
}
