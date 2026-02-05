package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface TransactionServicePort {
    Transaction create(Transaction transaction);

    Transaction update(Transaction transaction, UUID profileId);

    Transaction findByIdAndProfileId(UUID id, UUID profileId);

    void delete(UUID id, UUID profileId);

    Transaction uploadReceipt(UUID id, UUID profileId, InputStream content, String contentType, long size);

    Transaction deleteReceipt(UUID id, UUID profileId);

    PageDomain<Transaction> search(UUID profileId, TransactionFilter filter);

    void createBatch(List<Transaction> transactions);
}
