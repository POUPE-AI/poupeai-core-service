package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import io.github.poupeai.core.persistence.mapper.TransactionEntityMapper;
import io.github.poupeai.core.persistence.repository.BankAccountRepository;
import io.github.poupeai.core.persistence.repository.CategoryRepository;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import io.github.poupeai.core.persistence.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    private final TransactionRepository repository;
    private final TransactionEntityMapper mapper;

    private final ProfileRepository profileRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;
    private final CategoryRepository categoryRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public Transaction create(Transaction transaction) {
        TransactionEntity entity = mapper.toEntity(transaction);
        setRelationships(entity, transaction);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<Transaction> createAll(List<Transaction> transactions) {
        List<TransactionEntity> entities = transactions.stream().map(t -> {
            TransactionEntity entity = mapper.toEntity(t);
            setRelationships(entity, t);
            return entity;
        }).toList();

        return mapper.toDomainList(repository.saveAll(entities));
    }

    @Override
    public Transaction update(Transaction transaction) {
        return create(transaction);
    }

    @Override
    public Optional<Transaction> findByIdAndProfileId(UUID id, UUID profileId) {
        return repository.findByIdAndProfileUserId(id, profileId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Transaction> findAllByProfileId(UUID profileId) {
        return mapper.toDomainList(repository.findAllByProfileUserIdOrderByTransactionDateDesc(profileId));
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByPurchaseGroupUuid(UUID purchaseGroupUuid) {
        repository.deleteByPurchaseGroupUuid(purchaseGroupUuid);
    }

    @Override
    public void deleteByInvoiceId(UUID invoiceId) {
        repository.deleteByInvoiceId(invoiceId);
    }

    @Override
    public BigDecimal sumAmountByBankAccountIdAndType(UUID bankAccountId, TransactionType type) {
        return repository.sumAmountByBankAccountIdAndType(bankAccountId, type);
    }

    @Override
    public BigDecimal sumAmountByInvoiceId(UUID invoiceId) {
        return repository.sumAmountByInvoiceId(invoiceId);
    }

    @Override
    public List<Transaction> findByPurchaseGroupUuid(UUID purchaseGroupUuid) {
        return mapper.toDomainList(repository.findByPurchaseGroupUuid(purchaseGroupUuid));
    }

    @Override
    public List<Transaction> findByInvoiceId(UUID invoiceId) {
        return mapper.toDomainList(repository.findByInvoiceId(invoiceId));
    }

    @Override
    public List<Transaction> findByBankAccountId(UUID bankAccountId) {
        return mapper.toDomainList(repository.findByBankAccountId(bankAccountId));
    }

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return repository.existsByIdAndProfileUserId(id, profileId);
    }

    @Override
    public PageDomain<Transaction> search(UUID profileId, TransactionFilter filter) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(filter.getSortDirection()),
                filter.getSortBy()
        );
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<TransactionEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("profile").get("userId"), profileId));

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getPurchaseGroupUuid() != null) {
                predicates.add(cb.equal(root.get("purchaseGroupUuid"), filter.getPurchaseGroupUuid()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TransactionEntity> page = repository.findAll(spec, pageable);

        return PageDomain.<Transaction>builder()
                .content(mapper.toDomainList(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private void setRelationships(TransactionEntity entity, Transaction domain) {
        entity.setProfile(profileRepository.getReferenceById(domain.getProfileId()));

        if (domain.getBankAccountId() != null) {
            entity.setBankAccount(bankAccountRepository.getReferenceById(domain.getBankAccountId()));
        }
        if (domain.getCreditCardId() != null) {
            entity.setCreditCard(creditCardRepository.getReferenceById(domain.getCreditCardId()));
        }

        if (domain.getCategory() != null && domain.getCategory().getId() != null) {
            entity.setCategory(categoryRepository.getReferenceById(domain.getCategory().getId()));
        }

        if (domain.getInvoiceId() != null) {
            entity.setInvoice(invoiceRepository.getReferenceById(domain.getInvoiceId()));
        }
    }
}
