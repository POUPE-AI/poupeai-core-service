package io.github.poupeai.core.web.controller.transaction;

import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.web.dto.transaction.TransactionRequest;
import io.github.poupeai.core.web.dto.transaction.TransactionResponse;
import io.github.poupeai.core.web.dto.transaction.TransactionUpdateRequest;
import io.github.poupeai.core.web.mapper.transaction.TransactionControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionServicePort transactionServicePort;

    @Mock
    private TransactionControllerMapper transactionMapper;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    @DisplayName("Should get all transactions for user")
    void shouldGetTransactionsSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder().id(transactionId).profileId(userId).build();
        List<Transaction> transactions = List.of(transaction);
        TransactionResponse response = TransactionResponse.builder().id(transactionId).build();
        List<TransactionResponse> responses = List.of(response);

        when(transactionServicePort.findAllByProfileId(userId)).thenReturn(transactions);
        when(transactionMapper.toResponseList(transactions)).thenReturn(responses);

        ResponseEntity<List<TransactionResponse>> result = transactionController.getTransactions(userId.toString());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("Should get transaction by id successfully")
    void shouldGetTransactionByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder().id(transactionId).profileId(userId).build();
        TransactionResponse response = TransactionResponse.builder().id(transactionId).build();

        when(transactionServicePort.findByIdAndProfileId(transactionId, userId)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        ResponseEntity<TransactionResponse> result = transactionController.getTransactionById(userId.toString(), transactionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(transactionId, result.getBody().getId());
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionRequest request = TransactionRequest.builder()
                .description("Test Transaction")
                .amount(BigDecimal.valueOf(100))
                .transactionDate(LocalDate.now())
                .bankAccountId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .build();
        Transaction transaction = Transaction.builder().profileId(userId).build();
        Transaction savedTransaction = Transaction.builder().id(transactionId).profileId(userId).build();
        TransactionResponse response = TransactionResponse.builder()
                .id(transactionId)
                .description("Test Transaction")
                .amount(BigDecimal.valueOf(100))
                .type(TransactionType.EXPENSE)
                .build();

        when(transactionMapper.toDomain(eq(request), any(UUID.class))).thenReturn(transaction);
        when(transactionServicePort.create(transaction)).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(response);

        ResponseEntity<TransactionResponse> result = transactionController.createTransaction(userId.toString(), request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(transactionId, result.getBody().getId());
        verify(transactionServicePort).create(transaction);
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .description("Updated Description")
                .amount(BigDecimal.valueOf(200))
                .build();
        Transaction transaction = Transaction.builder().id(transactionId).profileId(userId).build();
        TransactionResponse response = TransactionResponse.builder()
                .id(transactionId)
                .description("Updated Description")
                .amount(BigDecimal.valueOf(200))
                .build();

        when(transactionServicePort.findByIdAndProfileId(transactionId, userId)).thenReturn(transaction);
        doNothing().when(transactionMapper).updateDomainFromDto(eq(request), eq(transaction));
        when(transactionServicePort.update(transaction, userId)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        ResponseEntity<TransactionResponse> result = transactionController.updateTransaction(userId.toString(), transactionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Updated Description", result.getBody().getDescription());
        verify(transactionServicePort).update(transaction, userId);
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void shouldDeleteTransactionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        ResponseEntity<Void> result = transactionController.deleteTransaction(userId.toString(), transactionId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(transactionServicePort).delete(transactionId, userId);
    }
}
