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
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    @DisplayName("Should persist credit card, link profile/institution and calculate used limit")
    void createShouldPersistWhenDataIsValid() {
        UUID profileId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Long institutionId = 1L;
        BigDecimal usedLimit = BigDecimal.valueOf(150.00);

        CreditCard domain = CreditCard.builder()
                .profileId(profileId)
                .institution(Institution.builder().id(institutionId).build())
                .build();

        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(cardId);

        ProfileEntity profileProxy = new ProfileEntity();
        InstitutionEntity institutionProxy = new InstitutionEntity();

        when(creditCardMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(institutionRepository.getReferenceById(institutionId)).thenReturn(institutionProxy);
        when(creditCardRepository.save(entity)).thenReturn(entity);

        when(creditCardMapper.toDomain(any(CreditCardEntity.class))).thenReturn(domain);
        when(invoiceRepository.calculateUsedCreditLimit(cardId)).thenReturn(usedLimit);

        CreditCard result = adapter.create(domain);

        assertNotNull(result);
        assertEquals(profileProxy, entity.getProfile());
        assertEquals(institutionProxy, entity.getInstitution());
        assertEquals(usedLimit, result.getUsedCreditLimit());

        verify(creditCardRepository).save(entity);
        verify(invoiceRepository).calculateUsedCreditLimit(cardId);
    }

    @Test
    @DisplayName("Should update credit card fields and recalculate used limit")
    void updateShouldUpdateFieldsWhenCreditCardExists() {
        UUID cardId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Long institutionId = 2L;
        BigDecimal usedLimit = BigDecimal.valueOf(500.00);

        CreditCard domain = CreditCard.builder()
                .id(cardId)
                .profileId(profileId)
                .name("Updated Card")
                .institution(Institution.builder().id(institutionId).build())
                .build();

        CreditCardEntity existingEntity = new CreditCardEntity();
        existingEntity.setId(cardId);

        InstitutionEntity institutionProxy = new InstitutionEntity();

        when(creditCardRepository.findByIdAndProfileUserId(cardId, profileId))
                .thenReturn(Optional.of(existingEntity));
        when(institutionRepository.getReferenceById(institutionId)).thenReturn(institutionProxy);
        when(creditCardRepository.save(existingEntity)).thenReturn(existingEntity);

        when(creditCardMapper.toDomain(any(CreditCardEntity.class))).thenReturn(domain);
        when(invoiceRepository.calculateUsedCreditLimit(cardId)).thenReturn(usedLimit);

        CreditCard result = adapter.update(domain);

        assertNotNull(result);
        assertEquals("Updated Card", existingEntity.getName());
        assertEquals(institutionProxy, existingEntity.getInstitution());
        assertEquals(usedLimit, result.getUsedCreditLimit());

        verify(creditCardRepository).save(existingEntity);
        verify(invoiceRepository).calculateUsedCreditLimit(cardId);
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
        verify(invoiceRepository, never()).calculateUsedCreditLimit(any());
    }

    @Test
    @DisplayName("Should return credit card with calculated limit when found by ID")
    void findByIdShouldReturnCardWhenFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        BigDecimal usedLimit = BigDecimal.valueOf(100.00);

        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(id);

        CreditCard domain = new CreditCard();

        when(creditCardRepository.findByIdAndProfileUserId(id, profileId)).thenReturn(Optional.of(entity));
        when(creditCardMapper.toDomain(entity)).thenReturn(domain);
        when(invoiceRepository.calculateUsedCreditLimit(id)).thenReturn(usedLimit);

        Optional<CreditCard> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        assertEquals(usedLimit, result.get().getUsedCreditLimit());
    }

    @Test
    @DisplayName("Should return list of credit cards with calculated limits for profile")
    void findAllByProfileIdShouldReturnList() {
        UUID profileId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        BigDecimal usedLimit = BigDecimal.TEN;

        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(cardId);
        List<CreditCardEntity> entities = List.of(entity);

        CreditCard domain = new CreditCard();

        when(creditCardRepository.findAllByProfileUserId(profileId)).thenReturn(entities);
        when(creditCardMapper.toDomain(entity)).thenReturn(domain);
        when(invoiceRepository.calculateUsedCreditLimit(cardId)).thenReturn(usedLimit);

        List<CreditCard> result = adapter.findAllByProfileId(profileId);

        assertEquals(1, result.size());
        assertEquals(usedLimit, result.get(0).getUsedCreditLimit());

        verify(creditCardMapper).toDomain(entity);
        verify(creditCardMapper, never()).toDomainList(any());
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