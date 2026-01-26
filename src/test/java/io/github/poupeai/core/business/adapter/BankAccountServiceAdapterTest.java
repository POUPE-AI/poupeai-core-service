package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ForbiddenActionException;
import io.github.poupeai.core.domain.exception.ResourceAlreadyExistsException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceAdapterTest {

    @Mock
    private BankAccountRepositoryPort bankAccountRepositoryPort;

    @Mock
    private InstitutionRepositoryPort institutionRepositoryPort;

    @InjectMocks
    private BankAccountServiceAdapter bankAccountServiceAdapter;

    @Test
    @DisplayName("Should create bank account successfully when data is valid")
    void shouldCreateBankAccountSuccessfully() {
        BankAccount account = BankAccount.builder()
                .profileId(UUID.randomUUID())
                .name("Nubank")
                .initialBalance(BigDecimal.valueOf(1000))
                .institutionId(1L)
                .build();

        when(institutionRepositoryPort.existsById(1L)).thenReturn(true);
        when(bankAccountRepositoryPort.isNameTaken(account.getName(), account.getProfileId(), null))
                .thenReturn(false);
        when(bankAccountRepositoryPort.countByProfileId(account.getProfileId())).thenReturn(0L);
        when(bankAccountRepositoryPort.create(any())).thenReturn(account);

        BankAccount result = bankAccountServiceAdapter.create(account);

        assertNotNull(result);
        assertEquals(account.getName(), result.getName());
        verify(bankAccountRepositoryPort).create(any());
    }

    @Test
    @DisplayName("Should set first account as default when creating")
    void shouldSetFirstAccountAsDefault() {
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .profileId(profileId)
                .name("Nubank")
                .isDefault(false)
                .build();

        when(bankAccountRepositoryPort.isNameTaken(account.getName(), profileId, null)).thenReturn(false);
        when(bankAccountRepositoryPort.countByProfileId(profileId)).thenReturn(0L);
        when(bankAccountRepositoryPort.create(any())).thenAnswer(inv -> inv.getArgument(0));

        BankAccount result = bankAccountServiceAdapter.create(account);

        assertTrue(result.getIsDefault());
    }

    @Test
    @DisplayName("Should clear other defaults when creating default account")
    void shouldClearOtherDefaultsWhenCreatingDefaultAccount() {
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .profileId(profileId)
                .name("Nubank")
                .isDefault(true)
                .build();

        when(bankAccountRepositoryPort.isNameTaken(account.getName(), profileId, null)).thenReturn(false);
        when(bankAccountRepositoryPort.countByProfileId(profileId)).thenReturn(1L);
        when(bankAccountRepositoryPort.create(any())).thenReturn(account);

        bankAccountServiceAdapter.create(account);

        verify(bankAccountRepositoryPort).clearDefaultByProfileId(profileId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when institution does not exist on create")
    void shouldThrowExceptionWhenInstitutionNotFoundOnCreate() {
        BankAccount account = BankAccount.builder()
                .institutionId(99L)
                .build();

        when(institutionRepositoryPort.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> bankAccountServiceAdapter.create(account));
        verify(bankAccountRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when name is taken on create")
    void shouldThrowExceptionWhenNameTakenOnCreate() {
        BankAccount account = BankAccount.builder()
                .profileId(UUID.randomUUID())
                .name("Nubank")
                .build();

        when(bankAccountRepositoryPort.isNameTaken(account.getName(), account.getProfileId(), null))
                .thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> bankAccountServiceAdapter.create(account));
        verify(bankAccountRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should throw DomainException when initial balance is negative")
    void shouldThrowExceptionWhenInitialBalanceIsNegative() {
        BankAccount account = BankAccount.builder()
                .profileId(UUID.randomUUID())
                .name("Nubank")
                .initialBalance(BigDecimal.valueOf(-100))
                .build();

        when(bankAccountRepositoryPort.isNameTaken(account.getName(), account.getProfileId(), null))
                .thenReturn(false);

        assertThrows(DomainException.class, () -> bankAccountServiceAdapter.create(account));
        verify(bankAccountRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should update bank account successfully when data is valid")
    void shouldUpdateBankAccountSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(id)
                .profileId(profileId)
                .name("Nubank Updated")
                .institutionId(1L)
                .build();

        when(institutionRepositoryPort.existsById(1L)).thenReturn(true);
        when(bankAccountRepositoryPort.isNameTaken(account.getName(), profileId, id))
                .thenReturn(false);
        when(bankAccountRepositoryPort.update(account)).thenReturn(account);

        BankAccount result = bankAccountServiceAdapter.update(account, profileId);

        assertNotNull(result);
        assertEquals("Nubank Updated", result.getName());
        verify(bankAccountRepositoryPort).update(account);
    }

    @Test
    @DisplayName("Should clear other defaults when updating to default account")
    void shouldClearOtherDefaultsWhenUpdatingToDefault() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(id)
                .profileId(profileId)
                .name("Nubank")
                .isDefault(true)
                .build();

        when(bankAccountRepositoryPort.isNameTaken(account.getName(), profileId, id)).thenReturn(false);
        when(bankAccountRepositoryPort.update(account)).thenReturn(account);

        bankAccountServiceAdapter.update(account, profileId);

        verify(bankAccountRepositoryPort).clearDefaultByProfileId(profileId);
    }

    @Test
    @DisplayName("Should find bank account by id successfully")
    void shouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder().id(id).build();

        when(bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.of(account));

        BankAccount result = bankAccountServiceAdapter.findByIdAndProfileId(id, profileId);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when bank account not found")
    void shouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bankAccountServiceAdapter.findByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should return all bank accounts for profile")
    void shouldReturnAllBankAccountsForProfile() {
        UUID profileId = UUID.randomUUID();
        List<BankAccount> accounts = List.of(
                BankAccount.builder().name("Nubank").build(),
                BankAccount.builder().name("Bradesco").build()
        );

        when(bankAccountRepositoryPort.findAllByProfileId(profileId)).thenReturn(accounts);

        List<BankAccount> result = bankAccountServiceAdapter.findAllByProfileId(profileId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should delete bank account successfully")
    void shouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(id)
                .profileId(profileId)
                .isDefault(false)
                .build();

        when(bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.of(account));

        bankAccountServiceAdapter.delete(id, profileId);

        verify(bankAccountRepositoryPort).delete(id);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent bank account")
    void shouldThrowExceptionOnDeleteNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bankAccountServiceAdapter.delete(id, profileId));
        verify(bankAccountRepositoryPort, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw ForbiddenActionException when deleting default account with other accounts")
    void shouldThrowExceptionWhenDeletingDefaultWithOtherAccounts() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(id)
                .profileId(profileId)
                .isDefault(true)
                .build();

        when(bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.of(account));
        when(bankAccountRepositoryPort.countByProfileId(profileId)).thenReturn(2L);

        assertThrows(ForbiddenActionException.class, () -> bankAccountServiceAdapter.delete(id, profileId));
        verify(bankAccountRepositoryPort, never()).delete(any());
    }

    @Test
    @DisplayName("Should allow deleting default account when it is the only one")
    void shouldAllowDeletingDefaultWhenOnlyAccount() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount account = BankAccount.builder()
                .id(id)
                .profileId(profileId)
                .isDefault(true)
                .build();

        when(bankAccountRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.of(account));
        when(bankAccountRepositoryPort.countByProfileId(profileId)).thenReturn(1L);

        bankAccountServiceAdapter.delete(id, profileId);

        verify(bankAccountRepositoryPort).delete(id);
    }
}
