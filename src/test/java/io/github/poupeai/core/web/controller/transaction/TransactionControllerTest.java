package io.github.poupeai.core.web.controller.transaction;

import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.TransactionServicePort;
import io.github.poupeai.core.web.dto.common.PageResponse;
import io.github.poupeai.core.web.dto.transaction.CreateTransactionRequest;
import io.github.poupeai.core.web.dto.transaction.TransactionResponse;
import io.github.poupeai.core.web.dto.transaction.UpdateTransactionRequest;
import io.github.poupeai.core.web.mapper.transaction.TransactionControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionServicePort transactionServicePort;

    @Mock
    private TransactionControllerMapper transactionMapper;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    @DisplayName("Should get paginated transactions with filters")
    void shouldGetTransactionsSuccessfully() {
        UUID userId = UUID.randomUUID();
        Transaction transaction = Transaction.builder().id(UUID.randomUUID()).build();

        PageDomain<Transaction> pageDomain = PageDomain.<Transaction>builder()
                .content(List.of(transaction))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        TransactionResponse responseDto = TransactionResponse.builder().id(transaction.getId()).build();

        when(transactionServicePort.search(eq(userId), any(TransactionFilter.class))).thenReturn(pageDomain);
        when(transactionMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        ResponseEntity<PageResponse<TransactionResponse>> result = transactionController.list(
                userId.toString(), 0, 10, TransactionType.EXPENSE, null, null, null, null, "ASC", "name"
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals(0, result.getBody().getPage());
        verify(transactionServicePort).search(eq(userId), any(TransactionFilter.class));
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

        ResponseEntity<TransactionResponse> result = transactionController.getById(userId.toString(), transactionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(transactionId, result.getBody().getId());
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        UUID userId = UUID.randomUUID();
        CreateTransactionRequest request = CreateTransactionRequest.builder().description("Test").build();
        Transaction transaction = Transaction.builder().description("Test").build();

        when(transactionMapper.toDomain(eq(request), eq(userId))).thenReturn(transaction);
        when(transactionServicePort.create(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(TransactionResponse.builder().description("Test").build());

        ResponseEntity<TransactionResponse> result = transactionController.create(userId.toString(), request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(transactionServicePort).create(transaction);
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UpdateTransactionRequest request = new UpdateTransactionRequest();

        Transaction partialTransaction = Transaction.builder().id(transactionId).build();
        Transaction updatedTransaction = Transaction.builder().id(transactionId).description("Updated").build();

        when(transactionMapper.toDomain(request, transactionId)).thenReturn(partialTransaction);
        when(transactionServicePort.update(partialTransaction, userId)).thenReturn(updatedTransaction);
        when(transactionMapper.toResponse(updatedTransaction)).thenReturn(new TransactionResponse());

        ResponseEntity<TransactionResponse> result = transactionController.update(userId.toString(), transactionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());

        verify(transactionMapper).toDomain(request, transactionId);
        verify(transactionServicePort).update(partialTransaction, userId);
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void shouldDeleteTransactionSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        ResponseEntity<Void> result = transactionController.delete(userId.toString(), transactionId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(transactionServicePort).delete(transactionId, userId);
    }

    @Test
    @DisplayName("Should upload receipt successfully")
    void shouldUploadReceiptSuccessfully() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);
        Transaction transaction = Transaction.builder().build();

        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024L);
        when(transactionServicePort.uploadReceipt(eq(transactionId), eq(userId), any(), anyString(), anyLong()))
                .thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(new TransactionResponse());

        ResponseEntity<TransactionResponse> result = transactionController.uploadReceipt(userId.toString(), transactionId, file);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(transactionServicePort).uploadReceipt(eq(transactionId), eq(userId), eq(inputStream), eq("image/png"), eq(1024L));
    }

    @Test
    @DisplayName("Should delete receipt successfully")
    void shouldDeleteReceiptSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.builder().build();

        when(transactionServicePort.deleteReceipt(transactionId, userId)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(new TransactionResponse());

        ResponseEntity<TransactionResponse> result = transactionController.deleteReceipt(userId.toString(), transactionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(transactionServicePort).deleteReceipt(transactionId, userId);
    }
}