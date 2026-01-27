package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.CategoryType;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.domain.port.output.StoragePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CategoryRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransactionServiceAdapter implements TransactionServicePort {
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CreditCardRepositoryPort creditCardRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final InvoiceServicePort invoiceServicePort;
    private final StoragePort storagePort;

    @Override
    @Transactional
    public Transaction create(Transaction transaction) {
        UUID categoryId = transaction.getCategory().getId();
        Category category = categoryRepositoryPort.findByIdAndProfileId(categoryId, transaction.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));

        Transaction enrichedTransaction = transaction.toBuilder()
                .category(category)
                .type(mapCategoryTypeToTransactionType(category.getType()))
                .build();

        enrichedTransaction.validateCreationState();
        validateExternalResources(enrichedTransaction);

        if (enrichedTransaction.getCreditCardId() != null) {
            return processCreditCardTransaction(enrichedTransaction);
        }

        return transactionRepositoryPort.create(enrichedTransaction);
    }

    @Override
    @Transactional
    public Transaction update(Transaction newData, UUID profileId) {
        Transaction existing = transactionRepositoryPort.findByIdAndProfileId(newData.getId(), profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));

        if (Boolean.TRUE.equals(existing.getIsInstallment())) {
            throw new DomainException("Não é possível editar uma transação parcelada individualmente. Delete e recrie.");
        }

        Category finalCategory = existing.getCategory();
        TransactionType finalType = existing.getType();

        if (newData.getCategory() != null && newData.getCategory().getId() != null) {
            UUID newCategoryId = newData.getCategory().getId();
            if (!newCategoryId.equals(existing.getCategory().getId())) {
                finalCategory = categoryRepositoryPort.findByIdAndProfileId(newCategoryId, profileId)
                        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
                finalType = mapCategoryTypeToTransactionType(finalCategory.getType());
            }
        }

        Transaction merged = existing.toBuilder()
                .description(newData.getDescription() != null ? newData.getDescription() : existing.getDescription())
                .amount(newData.getAmount() != null ? newData.getAmount() : existing.getAmount())
                .transactionDate(newData.getTransactionDate() != null ? newData.getTransactionDate() : existing.getTransactionDate())
                .category(finalCategory)
                .type(finalType)
                .attachmentKey(newData.getAttachmentKey() != null ? newData.getAttachmentKey() : existing.getAttachmentKey())
                .originalStatementId(newData.getOriginalStatementId() != null ? newData.getOriginalStatementId() : existing.getOriginalStatementId())
                .build();

        merged.validateCreationState();

        UUID oldInvoiceId = existing.getInvoiceId();
        Transaction updated = transactionRepositoryPort.update(merged);

        if (oldInvoiceId != null) invoiceServicePort.updateInvoiceTotals(oldInvoiceId);
        if (updated.getInvoiceId() != null) invoiceServicePort.updateInvoiceTotals(updated.getInvoiceId());

        return updated;
    }

    @Override
    public Transaction findByIdAndProfileId(UUID id, UUID profileId) {
        Transaction t = transactionRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));
        enrichWithUrl(t);
        return t;
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID profileId) {
        Transaction transaction = findByIdAndProfileId(id, profileId);

        if (transaction.getAttachmentKey() != null) {
            storagePort.delete(transaction.getAttachmentKey());
        }

        if (transaction.getAttachmentKey() != null) {
            storagePort.delete(transaction.getAttachmentKey());
        }

        if (Boolean.TRUE.equals(transaction.getIsInstallment()) && transaction.getPurchaseGroupUuid() != null) {
            List<Transaction> installments = transactionRepositoryPort.findByPurchaseGroupUuid(transaction.getPurchaseGroupUuid());
            transactionRepositoryPort.deleteByPurchaseGroupUuid(transaction.getPurchaseGroupUuid());

            installments.stream()
                    .map(Transaction::getInvoiceId)
                    .distinct()
                    .forEach(invoiceServicePort::updateInvoiceTotals);
        } else {
            transactionRepositoryPort.delete(id);
            if (transaction.getInvoiceId() != null) {
                invoiceServicePort.updateInvoiceTotals(transaction.getInvoiceId());
            }
        }
    }

    @Override
    public PageDomain<Transaction> search(UUID profileId, TransactionFilter filter) {
        if (filter.getSortBy() == null || filter.getSortBy().isEmpty()) {
            filter.setSortBy("transactionDate");
        }
        PageDomain<Transaction> page = transactionRepositoryPort.search(profileId, filter);

        page.getContent().forEach(this::enrichWithUrl);
        return page;
    }

    @Override
    @Transactional
    public Transaction uploadReceipt(UUID id, UUID profileId, InputStream content, String contentType, long size) {
        Set<String> allowedTypes = Set.of("image/jpeg", "image/jpg", "image/png", "application/pdf");
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new DomainException("Tipo de arquivo inválido. Use JPG, PNG ou PDF.");
        }

        Transaction transaction = findByIdAndProfileId(id, profileId);

        if (transaction.getAttachmentKey() != null) {
            storagePort.delete(transaction.getAttachmentKey());
        }

        String key = UUID.randomUUID().toString();

        Map<String, String> tags = Map.of(
                "transactionId", id.toString(),
                "profileId", profileId.toString());
        storagePort.upload(key, content, contentType, size, tags);

        Transaction updated = transaction.withAttachmentKey(key);
        Transaction saved = transactionRepositoryPort.update(updated);
        enrichWithUrl(saved);
        return saved;
    }

    @Override
    @Transactional
    public Transaction deleteReceipt(UUID id, UUID profileId) {
        Transaction transaction = findByIdAndProfileId(id, profileId);
        if (transaction.getAttachmentKey() == null) {
            throw new DomainException("Transação sem comprovante.");
        }
        storagePort.delete(transaction.getAttachmentKey());

        Transaction updated = transaction.withAttachmentKey(null);
        return transactionRepositoryPort.update(updated);
    }

    private Transaction processCreditCardTransaction(Transaction transaction) {
        CreditCard card = creditCardRepositoryPort.findByIdAndProfileId(
                        transaction.getCreditCardId(), transaction.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));

        List<Transaction> transactionsToSave = transaction.generateInstallments();

        List<Transaction> transactionsWithInvoices = transactionsToSave.stream()
                .map(t -> {
                    Invoice invoice = invoiceServicePort.getOrCreateInvoiceForDate(card, t.getTransactionDate());
                    return t.withInvoiceId(invoice.getId());
                })
                .toList();

        List<Transaction> saved = transactionRepositoryPort.createAll(transactionsWithInvoices);

        transactionsWithInvoices.stream()
                .map(Transaction::getInvoiceId)
                .distinct()
                .forEach(invoiceServicePort::updateInvoiceTotals);

        return saved.getFirst();
    }

    private void validateExternalResources(Transaction t) {
        if (t.getBankAccountId() != null) {
            if (!bankAccountRepositoryPort.existsByIdAndProfileId(t.getBankAccountId(), t.getProfileId())) {
                throw new ResourceNotFoundException("Conta bancária não encontrada.");
            }
        }
    }

    private TransactionType mapCategoryTypeToTransactionType(CategoryType categoryType) {
        return categoryType == CategoryType.INCOME ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    private void enrichWithUrl(Transaction t) {
        if (t.getAttachmentKey() != null) {
            t.setAttachmentUrl(storagePort.generatePresignedUrl(t.getAttachmentKey()));
        }
    }
}
