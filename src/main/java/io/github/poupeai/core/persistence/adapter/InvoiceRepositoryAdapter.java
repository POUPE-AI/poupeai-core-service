package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import io.github.poupeai.core.persistence.mapper.InvoiceEntityMapper;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceEntityMapper invoiceMapper;
    private final CreditCardRepository creditCardRepository;

    @Override
    public Invoice create(Invoice invoice) {
        var entity = invoiceMapper.toEntity(invoice);
        
        var creditCard = creditCardRepository.getReferenceById(invoice.getCreditCardId());
        entity.setCreditCard(creditCard);
        
        var savedEntity = invoiceRepository.save(entity);
        return invoiceMapper.toDomain(savedEntity);
    }

    @Override
    public Invoice update(Invoice invoice) {
        var existingEntity = invoiceRepository.findById(invoice.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));
        
        existingEntity.setTotalAmount(invoice.getTotalAmount());
        existingEntity.setPaidAmount(invoice.getPaidAmount());
        existingEntity.setStatus(invoice.getStatus());
        existingEntity.setDueSoonNotificationSent(invoice.getDueSoonNotificationSent());
        existingEntity.setOverdueNotificationSent(invoice.getOverdueNotificationSent());
        
        var savedEntity = invoiceRepository.save(existingEntity);
        return invoiceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return invoiceRepository.findById(id)
                .map(invoiceMapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByIdAndProfileId(UUID id, UUID profileId) {
        return invoiceRepository.findByIdAndCreditCardProfileUserId(id, profileId)
                .map(invoiceMapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year) {
        return invoiceRepository.findByCreditCardIdAndMonthAndYear(creditCardId, month, year)
                .map(invoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findByCreditCardId(UUID creditCardId) {
        var entities = invoiceRepository.findByCreditCardId(creditCardId);
        return invoiceMapper.toDomainList(entities);
    }

    @Override
    public List<Invoice> findByProfileId(UUID profileId) {
        var entities = invoiceRepository.findByCreditCardProfileUserId(profileId);
        return invoiceMapper.toDomainList(entities);
    }

    @Override
    public void delete(UUID id) {
        invoiceRepository.deleteById(id);
    }

    @Override
    public boolean existsByCreditCardIdAndMonthAndYear(UUID creditCardId, Integer month, Integer year) {
        return invoiceRepository.existsByCreditCardIdAndMonthAndYear(creditCardId, month, year);
    }
}
