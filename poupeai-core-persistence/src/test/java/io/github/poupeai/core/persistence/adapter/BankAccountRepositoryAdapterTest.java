package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.persistence.entity.BankAccountEntity;
import io.github.poupeai.core.persistence.entity.InstitutionEntity;
import io.github.poupeai.core.persistence.entity.ProfileEntity;
import io.github.poupeai.core.persistence.mapper.BankAccountEntityMapper;
import io.github.poupeai.core.persistence.repository.BankAccountRepository;
import io.github.poupeai.core.persistence.repository.InstitutionRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountRepositoryAdapterTest {

    @InjectMocks
    private BankAccountRepositoryAdapter adapter;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankAccountEntityMapper bankAccountMapper;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Test
    @DisplayName("Should persist bank account and link profile and institution when data is valid")
    void createShouldPersistWhenDataIsValid() {
        UUID profileId = UUID.randomUUID();
        Long institutionId = 1L;
        BankAccount domain = BankAccount.builder()
                .profileId(profileId)
                .institutionId(institutionId)
                .build();

        BankAccountEntity entity = new BankAccountEntity();
        ProfileEntity profileProxy = new ProfileEntity();
        InstitutionEntity institutionProxy = new InstitutionEntity();

        when(bankAccountMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(institutionRepository.getReferenceById(institutionId)).thenReturn(institutionProxy);
        when(bankAccountRepository.save(entity)).thenReturn(entity);
        when(bankAccountMapper.toDomain(entity)).thenReturn(domain);

        BankAccount result = adapter.create(domain);

        assertNotNull(result);
        verify(bankAccountRepository).save(entity);
        assertEquals(profileProxy, entity.getProfile());
        assertEquals(institutionProxy, entity.getInstitution());
    }

    @Test
    @DisplayName("Should create bank account without institution when institution is null")
    void createShouldWorkWithoutInstitution() {
        UUID profileId = UUID.randomUUID();
        BankAccount domain = BankAccount.builder()
                .profileId(profileId)
                .institutionId(null)
                .build();

        BankAccountEntity entity = new BankAccountEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(bankAccountMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(bankAccountRepository.save(entity)).thenReturn(entity);
        when(bankAccountMapper.toDomain(entity)).thenReturn(domain);

        BankAccount result = adapter.create(domain);

        assertNotNull(result);
        verify(institutionRepository, never()).getReferenceById(any());
        assertNull(entity.getInstitution());
    }

    @Test
    @DisplayName("Should update bank account fields when bank account exists")
    void updateShouldUpdateFieldsWhenBankAccountExists() {
        UUID accountId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Long institutionId = 2L;

        BankAccount domain = BankAccount.builder()
                .id(accountId)
                .profileId(profileId)
                .name("Updated Account")
                .description("Updated description")
                .isDefault(true)
                .institutionId(institutionId)
                .build();

        BankAccountEntity existingEntity = new BankAccountEntity();
        InstitutionEntity institutionProxy = new InstitutionEntity();

        when(bankAccountRepository.findByIdAndProfileUserId(accountId, profileId))
                .thenReturn(Optional.of(existingEntity));
        when(institutionRepository.getReferenceById(institutionId)).thenReturn(institutionProxy);
        when(bankAccountRepository.save(existingEntity)).thenReturn(existingEntity);
        when(bankAccountMapper.toDomain(existingEntity)).thenReturn(domain);

        BankAccount result = adapter.update(domain);

        assertNotNull(result);
        assertEquals("Updated Account", existingEntity.getName());
        assertEquals("Updated description", existingEntity.getDescription());
        assertTrue(existingEntity.getIsDefault());
        assertEquals(institutionProxy, existingEntity.getInstitution());
        verify(bankAccountRepository).save(existingEntity);
    }

    @Test
    @DisplayName("Should clear institution when updating with null institution")
    void updateShouldClearInstitutionWhenNull() {
        UUID accountId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        BankAccount domain = BankAccount.builder()
                .id(accountId)
                .profileId(profileId)
                .name("Account")
                .institutionId(null)
                .build();

        BankAccountEntity existingEntity = BankAccountEntity.builder()
                .institution(new InstitutionEntity())
                .build();

        when(bankAccountRepository.findByIdAndProfileUserId(accountId, profileId))
                .thenReturn(Optional.of(existingEntity));
        when(bankAccountRepository.save(existingEntity)).thenReturn(existingEntity);
        when(bankAccountMapper.toDomain(existingEntity)).thenReturn(domain);

        adapter.update(domain);

        assertNull(existingEntity.getInstitution());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when bank account does not exist during update")
    void updateShouldThrowNotFoundWhenDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccount domain = BankAccount.builder()
                .id(accountId)
                .profileId(profileId)
                .build();

        when(bankAccountRepository.findByIdAndProfileUserId(accountId, profileId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adapter.update(domain));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return bank account when found by ID and Profile ID")
    void findByIdShouldReturnAccountWhenFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BankAccountEntity entity = new BankAccountEntity();
        BankAccount domain = new BankAccount();

        when(bankAccountRepository.findByIdAndProfileUserId(id, profileId)).thenReturn(Optional.of(entity));
        when(bankAccountMapper.toDomain(entity)).thenReturn(domain);

        Optional<BankAccount> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should return empty when bank account not found")
    void findByIdShouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(bankAccountRepository.findByIdAndProfileUserId(id, profileId)).thenReturn(Optional.empty());

        Optional<BankAccount> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return list of bank accounts for profile")
    void findAllByProfileIdShouldReturnList() {
        UUID profileId = UUID.randomUUID();
        List<BankAccountEntity> entities = List.of(new BankAccountEntity());
        List<BankAccount> domains = List.of(new BankAccount());

        when(bankAccountRepository.findAllByProfileUserId(profileId)).thenReturn(entities);
        when(bankAccountMapper.toDomainList(entities)).thenReturn(domains);

        List<BankAccount> result = adapter.findAllByProfileId(profileId);

        assertEquals(1, result.size());
        assertEquals(domains, result);
    }

    @Test
    @DisplayName("Should delete bank account by ID")
    void deleteShouldCallRepository() {
        UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(bankAccountRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should check if bank account exists for profile")
    void existsByIdAndProfileIdShouldReturnRepositoryResult() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(bankAccountRepository.existsByIdAndProfileUserId(id, profileId)).thenReturn(true);

        assertTrue(adapter.existsByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should check if name is taken for profile without excludeId")
    void isNameTakenShouldReturnRepositoryResultWithoutExcludeId() {
        String name = "Nubank";
        UUID profileId = UUID.randomUUID();
        when(bankAccountRepository.existsByNameAndProfileUserId(name, profileId)).thenReturn(true);

        assertTrue(adapter.isNameTaken(name, profileId, null));
    }

    @Test
    @DisplayName("Should check if name is taken for profile with excludeId")
    void isNameTakenShouldReturnRepositoryResultWithExcludeId() {
        String name = "Nubank";
        UUID profileId = UUID.randomUUID();
        UUID excludeId = UUID.randomUUID();
        when(bankAccountRepository.existsByNameAndProfileUserIdAndIdNot(name, profileId, excludeId)).thenReturn(true);

        assertTrue(adapter.isNameTaken(name, profileId, excludeId));
    }

    @Test
    @DisplayName("Should find default bank account for profile")
    void findDefaultByProfileIdShouldReturnAccount() {
        UUID profileId = UUID.randomUUID();
        BankAccountEntity entity = new BankAccountEntity();
        BankAccount domain = BankAccount.builder().isDefault(true).build();

        when(bankAccountRepository.findByProfileUserIdAndIsDefaultTrue(profileId)).thenReturn(Optional.of(entity));
        when(bankAccountMapper.toDomain(entity)).thenReturn(domain);

        Optional<BankAccount> result = adapter.findDefaultByProfileId(profileId);

        assertTrue(result.isPresent());
        assertTrue(result.get().getIsDefault());
    }

    @Test
    @DisplayName("Should count bank accounts for profile")
    void countByProfileIdShouldReturnCount() {
        UUID profileId = UUID.randomUUID();
        when(bankAccountRepository.countByProfileUserId(profileId)).thenReturn(5L);

        assertEquals(5L, adapter.countByProfileId(profileId));
    }

    @Test
    @DisplayName("Should clear default flag for all accounts of profile")
    void clearDefaultByProfileIdShouldCallRepository() {
        UUID profileId = UUID.randomUUID();

        adapter.clearDefaultByProfileId(profileId);

        verify(bankAccountRepository).clearDefaultByProfileUserId(profileId);
    }
}
