package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    private final TransactionRepository transactionRepository;
    private final TransactionEntityMapper transactionMapper;
    private final ProfileRepository profileRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;
    private final CategoryRepository categoryRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public Transaction create(Transaction transaction) {
        var entity = transactionMapper.toEntity(transaction);
        setRelationships(entity, transaction);
        var savedEntity = transactionRepository.save(entity);
        return transactionMapper.toDomain(savedEntity);
    }

    @Override
    public Transaction update(Transaction transaction) {
        var existingEntity = transactionRepository.findByIdAndProfileUserId(transaction.getId(), transaction.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));

        existingEntity.setDescription(transaction.getDescription());
        existingEntity.setAmount(transaction.getAmount());
        existingEntity.setType(transaction.getType());
        existingEntity.setTransactionDate(transaction.getTransactionDate());
        existingEntity.setAttachmentKey(transaction.getAttachmentKey());
        existingEntity.setOriginalStatementId(transaction.getOriginalStatementId());
        existingEntity.setOriginalStatementDescription(transaction.getOriginalStatementDescription());

        if (transaction.getBankAccountId() != null) {
            existingEntity.setBankAccount(bankAccountRepository.getReferenceById(transaction.getBankAccountId()));
        } else {
            existingEntity.setBankAccount(null);
        }

        if (transaction.getCreditCardId() != null) {
            existingEntity.setCreditCard(creditCardRepository.getReferenceById(transaction.getCreditCardId()));
        } else {
            existingEntity.setCreditCard(null);
        }

        if (transaction.getCategoryId() != null) {
            existingEntity.setCategory(categoryRepository.getReferenceById(transaction.getCategoryId()));
        } else {
            existingEntity.setCategory(null);
        }

        if (transaction.getInvoiceId() != null) {
            existingEntity.setInvoice(invoiceRepository.getReferenceById(transaction.getInvoiceId()));
        } else {
            existingEntity.setInvoice(null);
        }

        var savedEntity = transactionRepository.save(existingEntity);
        return transactionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdAndProfileId(UUID id, UUID profileId) {
        return transactionRepository.findByIdAndProfileUserId(id, profileId)
                .map(transactionMapper::toDomain);
    }

    @Override
    public List<Transaction> findAllByProfileId(UUID profileId) {
        var entities = transactionRepository.findAllByProfileUserIdOrderByTransactionDateDesc(profileId);
        return transactionMapper.toDomainList(entities);
    }

    @Override
    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return transactionRepository.existsByIdAndProfileUserId(id, profileId);
    }

    @Override
    public List<Transaction> findByPurchaseGroupUuid(UUID purchaseGroupUuid) {
        var entities = transactionRepository.findByPurchaseGroupUuid(purchaseGroupUuid);
        return transactionMapper.toDomainList(entities);
    }

    @Override
    public List<Transaction> findByInvoiceId(UUID invoiceId) {
        var entities = transactionRepository.findByInvoiceId(invoiceId);
        return transactionMapper.toDomainList(entities);
    }

    @Override
    public List<Transaction> findByBankAccountId(UUID bankAccountId) {
        var entities = transactionRepository.findByBankAccountId(bankAccountId);
        return transactionMapper.toDomainList(entities);
    }

    @Override
    public BigDecimal sumAmountByBankAccountIdAndType(UUID bankAccountId, TransactionType type) {
        return transactionRepository.sumAmountByBankAccountIdAndType(bankAccountId, type);
    }

    @Override
    public BigDecimal sumAmountByInvoiceId(UUID invoiceId) {
        return transactionRepository.sumAmountByInvoiceId(invoiceId);
    }

    @Override
    @Transactional
    public void deleteByPurchaseGroupUuid(UUID purchaseGroupUuid) {
        transactionRepository.deleteByPurchaseGroupUuid(purchaseGroupUuid);
    }

    @Override
    @Transactional
    public void deleteByInvoiceId(UUID invoiceId) {
        transactionRepository.deleteByInvoiceId(invoiceId);
    }

    @Override
    @Transactional
    public List<Transaction> createAll(List<Transaction> transactions) {
        List<Transaction> savedTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            var entity = transactionMapper.toEntity(transaction);
            setRelationships(entity, transaction);
            var savedEntity = transactionRepository.save(entity);
            savedTransactions.add(transactionMapper.toDomain(savedEntity));
        }
        return savedTransactions;
    }

    private void setRelationships(TransactionEntity entity, Transaction transaction) {
        var profile = profileRepository.getReferenceById(transaction.getProfileId());
        entity.setProfile(profile);

        if (transaction.getBankAccountId() != null) {
            entity.setBankAccount(bankAccountRepository.getReferenceById(transaction.getBankAccountId()));
        }

        if (transaction.getCreditCardId() != null) {
            entity.setCreditCard(creditCardRepository.getReferenceById(transaction.getCreditCardId()));
        }

        if (transaction.getCategoryId() != null) {
            entity.setCategory(categoryRepository.getReferenceById(transaction.getCategoryId()));
        }

        if (transaction.getInvoiceId() != null) {
            entity.setInvoice(invoiceRepository.getReferenceById(transaction.getInvoiceId()));
        }
    }

    @Override
    public PageDomain<Transaction> search(UUID profileId, TransactionFilter filter) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(filter.getSortDirection() != null ? filter.getSortDirection() : "ASC"),
                filter.getSortBy() != null ? filter.getSortBy() : "description"
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<TransactionEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("profile").get("userId"), profileId));

            if (filter.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.getType()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getPurchaseGroupUuid() != null) {
                predicates.add(criteriaBuilder.equal(root.get("purchaseGroup").get("id"), filter.getPurchaseGroupUuid()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<TransactionEntity> pageResult = transactionRepository.findAll(spec, pageable);
        List<Transaction> domainContent = transactionMapper.toDomainList(pageResult.getContent());

        return PageDomain.<Transaction>builder()
                .content(domainContent)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }
}
