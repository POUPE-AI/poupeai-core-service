package io.github.poupeai.core.domain.port.business;

import io.github.poupeai.core.domain.model.Transaction;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface TransactionServicePort {
    Transaction create(Transaction transaction);

    Transaction update(Transaction transaction, UUID profileId);

    Transaction findByIdAndProfileId(UUID id, UUID profileId);

    List<Transaction> findAllByProfileId(UUID profileId);

    void delete(UUID id, UUID profileId);

    Transaction uploadReceipt(UUID id, UUID profileId, InputStream content, String contentType, long size);
}
