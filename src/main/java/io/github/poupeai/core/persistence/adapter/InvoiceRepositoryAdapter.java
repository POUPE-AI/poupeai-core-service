package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceFilter;
import io.github.poupeai.core.domain.model.InvoiceNotificationData;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import io.github.poupeai.core.persistence.mapper.InvoiceEntityMapper;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Override
    public List<Invoice> findDueSoonNotNotified(LocalDate startDate, LocalDate endDate) {
        var entities = invoiceRepository.findDueSoonNotNotified(startDate, endDate);
        return invoiceMapper.toDomainList(entities);
    }

    @Override
    public List<Invoice> findOverdueNotNotified(LocalDate today) {
        var entities = invoiceRepository.findOverdueNotNotified(today);
        return invoiceMapper.toDomainList(entities);
    }

    @Override
    public List<InvoiceNotificationData> findDueSoonNotificationsData(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findDueSoonNotificationsData(startDate, endDate);
    }

    @Override
    public List<InvoiceNotificationData> findOverdueNotificationsData(LocalDate today) {
        return invoiceRepository.findOverdueNotificationsData(today);
    }

    @Override
    public PageDomain<Invoice> search(UUID profileId, InvoiceFilter filter) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(filter.getSortDirection()),
                filter.getSortBy()
        );
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<InvoiceEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("creditCard").get("profile").get("userId"), profileId));

            if (filter.getCreditCardId() != null) {
                predicates.add(cb.equal(root.get("creditCard").get("id"), filter.getCreditCardId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getMonth() != null) {
                predicates.add(cb.equal(root.get("month"), filter.getMonth()));
            }
            if (filter.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), filter.getYear()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InvoiceEntity> page = invoiceRepository.findAll(spec, pageable);

        return PageDomain.<Invoice>builder()
                .content(invoiceMapper.toDomainList(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
