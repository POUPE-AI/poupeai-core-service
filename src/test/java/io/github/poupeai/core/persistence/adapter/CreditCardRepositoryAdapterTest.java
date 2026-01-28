package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.persistence.entity.CreditCardEntity;
import io.github.poupeai.core.persistence.entity.InstitutionEntity;
import io.github.poupeai.core.persistence.entity.ProfileEntity;
import io.github.poupeai.core.persistence.mapper.CreditCardEntityMapper;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
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
class CreditCardRepositoryAdapterTest {

    @InjectMocks
    private CreditCardRepositoryAdapter adapter;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private CreditCardEntityMapper creditCardMapper;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Test
    @DisplayName("Should persist credit card and link profile and institution when data is valid")
    void createShouldPersistWhenDataIsValid() {
        UUID profileId = UUID.randomUUID();
        Long institutionId = 1L;
        CreditCard domain = CreditCard.builder()
                .profileId(profileId)
                .institution(Institution.builder().id(institutionId).build())
                .build();

        CreditCardEntity entity = new CreditCardEntity();
        ProfileEntity profileProxy = new ProfileEntity();
        InstitutionEntity institutionProxy = new InstitutionEntity();

        when(creditCardMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(institutionRepository.getReferenceById(institutionId)).thenReturn(institutionProxy);
        when(creditCardRepository.save(entity)).thenReturn(entity);
        when(creditCardMapper.toDomain(entity)).thenReturn(domain);

        CreditCard result = adapter.create(domain);

        assertNotNull(result);
        verify(creditCardRepository).save(entity);
        assertEquals(profileProxy, entity.getProfile());
        assertEquals(institutionProxy, entity.getInstitution());
    }

    @Test
    @DisplayName("Should update credit card fields when credit card exists")
    void updateShouldUpdateFieldsWhenCreditCardExists() {
        UUID cardId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Long institutionId = 2L;

        CreditCard domain = CreditCard.builder()
                .id(cardId)
                .profileId(profileId)
                .name("Updated Card")
                .institution(Institution.builder().id(institutionId).build())
                .build();

        CreditCardEntity existingEntity = new CreditCardEntity();
        InstitutionEntity institutionProxy = new InstitutionEntity();

        when(creditCardRepository.findByIdAndProfileUserId(cardId, profileId))
                .thenReturn(Optional.of(existingEntity));
        when(institutionRepository.getReferenceById(institutionId)).thenReturn(institutionProxy);
        when(creditCardRepository.save(existingEntity)).thenReturn(existingEntity);
        when(creditCardMapper.toDomain(existingEntity)).thenReturn(domain);

        CreditCard result = adapter.update(domain);

        assertNotNull(result);
        assertEquals("Updated Card", existingEntity.getName());
        assertEquals(institutionProxy, existingEntity.getInstitution());
        verify(creditCardRepository).save(existingEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when credit card does not exist during update")
    void updateShouldThrowNotFoundWhenDoesNotExist() {
        UUID cardId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        CreditCard domain = CreditCard.builder()
                .id(cardId)
                .profileId(profileId)
                .build();

        when(creditCardRepository.findByIdAndProfileUserId(cardId, profileId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adapter.update(domain));
        verify(creditCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return credit card when found by ID and Profile ID")
    void findByIdShouldReturnCardWhenFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        CreditCardEntity entity = new CreditCardEntity();
        CreditCard domain = new CreditCard();

        when(creditCardRepository.findByIdAndProfileUserId(id, profileId)).thenReturn(Optional.of(entity));
        when(creditCardMapper.toDomain(entity)).thenReturn(domain);

        Optional<CreditCard> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should return list of credit cards for profile")
    void findAllByProfileIdShouldReturnList() {
        UUID profileId = UUID.randomUUID();
        List<CreditCardEntity> entities = List.of(new CreditCardEntity());
        List<CreditCard> domains = List.of(new CreditCard());

        when(creditCardRepository.findAllByProfileUserId(profileId)).thenReturn(entities);
        when(creditCardMapper.toDomainList(entities)).thenReturn(domains);

        List<CreditCard> result = adapter.findAllByProfileId(profileId);

        assertEquals(1, result.size());
        assertEquals(domains, result);
    }

    @Test
    @DisplayName("Should delete credit card by ID")
    void deleteShouldCallRepository() {
        UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(creditCardRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should check if credit card exists for profile")
    void existsByIdAndProfileIdShouldReturnRepositoryResult() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(creditCardRepository.existsByIdAndProfileUserId(id, profileId)).thenReturn(true);

        assertTrue(adapter.existsByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should check if name is taken for profile")
    void isNameTakenShouldReturnRepositoryResult() {
        String name = "Nubank";
        UUID profileId = UUID.randomUUID();
        when(creditCardRepository.existsByNameAndProfileUserId(name, profileId)).thenReturn(true);

        assertTrue(adapter.isNameTaken(name, profileId, null));
    }
}
