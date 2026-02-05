package io.github.poupeai.core.web.controller.bankaccount;

import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.port.business.BankAccountServicePort;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountRequest;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountResponse;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountUpdateRequest;
import io.github.poupeai.core.web.mapper.bankaccount.BankAccountControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {

    @Mock
    private BankAccountServicePort bankAccountServicePort;

    @Mock
    private BankAccountControllerMapper bankAccountMapper;

    @InjectMocks
    private BankAccountController bankAccountController;

    @Test
    @DisplayName("Should get all bank accounts for user")
    void shouldGetBankAccountsSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        BankAccount bankAccount = BankAccount.builder().id(accountId).profileId(userId).build();
        List<BankAccount> bankAccounts = List.of(bankAccount);
        BankAccountResponse response = BankAccountResponse.builder()
                .id(accountId)
                .build();
        List<BankAccountResponse> responses = List.of(response);

        when(bankAccountServicePort.findAllByProfileId(userId)).thenReturn(bankAccounts);
        when(bankAccountMapper.toResponseList(bankAccounts)).thenReturn(responses);
        when(bankAccountServicePort.calculateCurrentBalance(accountId, userId)).thenReturn(BigDecimal.valueOf(1000));

        ResponseEntity<List<BankAccountResponse>> result = bankAccountController.list(userId.toString());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(BigDecimal.valueOf(1000), result.getBody().get(0).getCurrentBalance());
    }

    @Test
    @DisplayName("Should get bank account by id successfully")
    void shouldGetBankAccountByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        BankAccount account = BankAccount.builder().id(accountId).profileId(userId).build();
        BankAccountResponse response = BankAccountResponse.builder()
                .id(accountId)
                .build();

        when(bankAccountServicePort.findByIdAndProfileId(accountId, userId)).thenReturn(account);
        when(bankAccountMapper.toResponse(account)).thenReturn(response);
        when(bankAccountServicePort.calculateCurrentBalance(accountId, userId)).thenReturn(BigDecimal.valueOf(500));

        ResponseEntity<BankAccountResponse> result = bankAccountController.getById(userId.toString(), accountId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BigDecimal.valueOf(500), result.getBody().getCurrentBalance());
    }

    @Test
    @DisplayName("Should create bank account successfully")
    void shouldCreateBankAccountSuccessfully() {
        String userId = UUID.randomUUID().toString();
        BankAccountRequest request = BankAccountRequest.builder()
                .name("Nubank")
                .initialBalance(BigDecimal.valueOf(1000))
                .build();
        BankAccount account = new BankAccount();
        BankAccount savedAccount = new BankAccount();
        BankAccountResponse response = BankAccountResponse.builder()
                .name("Nubank")
                .initialBalance(BigDecimal.valueOf(1000))
                .build();

        when(bankAccountMapper.toDomain(eq(request), any(UUID.class))).thenReturn(account);
        when(bankAccountServicePort.create(account)).thenReturn(savedAccount);
        when(bankAccountMapper.toResponse(savedAccount)).thenReturn(response);

        ResponseEntity<BankAccountResponse> result = bankAccountController.create(userId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(bankAccountServicePort).create(account);
    }

    @Test
    @DisplayName("Should update bank account successfully")
    void shouldUpdateBankAccountSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        BankAccountUpdateRequest request = BankAccountUpdateRequest.builder()
                .name("Updated Name")
                .isDefault(true)
                .build();
        BankAccount account = BankAccount.builder().id(accountId).profileId(userId).build();
        BankAccountResponse response = BankAccountResponse.builder()
                .id(accountId)
                .name("Updated Name")
                .isDefault(true)
                .build();

        when(bankAccountServicePort.findByIdAndProfileId(accountId, userId)).thenReturn(account);
        doNothing().when(bankAccountMapper).updateDomainFromDto(eq(request), eq(account));
        when(bankAccountServicePort.update(account, userId)).thenReturn(account);
        when(bankAccountMapper.toResponse(account)).thenReturn(response);

        ResponseEntity<BankAccountResponse> result = bankAccountController.update(userId.toString(), accountId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(bankAccountServicePort).update(account, userId);
    }

    @Test
    @DisplayName("Should delete bank account successfully")
    void shouldDeleteBankAccountSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        ResponseEntity<Void> result = bankAccountController.delete(userId.toString(), accountId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(bankAccountServicePort).delete(accountId, userId);
    }
}
