package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceAlreadyExistsException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceAdapterTest {

    @Mock
    private CreditCardRepositoryPort creditCardRepositoryPort;

    @Mock
    private InstitutionRepositoryPort institutionRepositoryPort;

    @InjectMocks
    private CreditCardServiceAdapter creditCardServiceAdapter;

    @Test
    @DisplayName("Should create credit card successfully when data is valid")
    void shouldCreateCreditCardSuccessfully() {
        CreditCard card = CreditCard.builder()
                .profileId(UUID.randomUUID())
                .name("Nubank")
                .closingDay(5)
                .dueDay(15)
                .institution(Institution.builder().id(1L).build())
                .build();

        when(institutionRepositoryPort.existsById(1L)).thenReturn(true);
        when(creditCardRepositoryPort.isNameTaken(card.getName(), card.getProfileId(), null))
                .thenReturn(false);
        when(creditCardRepositoryPort.create(card)).thenReturn(card);

        CreditCard result = creditCardServiceAdapter.create(card);

        assertNotNull(result);
        assertEquals(card.getName(), result.getName());
        verify(creditCardRepositoryPort).create(card);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when institution does not exist on create")
    void shouldThrowExceptionWhenInstitutionNotFoundOnCreate() {
        CreditCard card = CreditCard.builder()
                .institution(Institution.builder().id(99L).build())
                .build();

        when(institutionRepositoryPort.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> creditCardServiceAdapter.create(card));
        verify(creditCardRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should throw DomainException when closing and due days are equal on create")
    void shouldThrowExceptionWhenDaysAreEqualOnCreate() {
        CreditCard card = CreditCard.builder()
                .closingDay(10)
                .dueDay(10)
                .build();

        assertThrows(DomainException.class, () -> creditCardServiceAdapter.create(card));
        verify(creditCardRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when name is taken on create")
    void shouldThrowExceptionWhenNameTakenOnCreate() {
        CreditCard card = CreditCard.builder()
                .profileId(UUID.randomUUID())
                .name("Nubank")
                .closingDay(5)
                .dueDay(15)
                .build();

        when(creditCardRepositoryPort.isNameTaken(card.getName(), card.getProfileId(), null))
                .thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> creditCardServiceAdapter.create(card));
        verify(creditCardRepositoryPort, never()).create(any());
    }

    @Test
    @DisplayName("Should update credit card successfully when data is valid")
    void shouldUpdateCreditCardSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        CreditCard card = CreditCard.builder()
                .id(id)
                .profileId(profileId)
                .name("Nubank Updated")
                .closingDay(5)
                .dueDay(15)
                .institution(Institution.builder().id(1L).build())
                .build();

        when(institutionRepositoryPort.existsById(1L)).thenReturn(true);
        when(creditCardRepositoryPort.isNameTaken(card.getName(), profileId, id))
                .thenReturn(false);
        when(creditCardRepositoryPort.update(card)).thenReturn(card);

        CreditCard result = creditCardServiceAdapter.update(card, profileId);

        assertNotNull(result);
        assertEquals("Nubank Updated", result.getName());
        verify(creditCardRepositoryPort).update(card);
    }

    @Test
    @DisplayName("Should throw DomainException when closing and due days are equal on update")
    void shouldThrowExceptionWhenDaysAreEqualOnUpdate() {
        UUID profileId = UUID.randomUUID();
        CreditCard card = CreditCard.builder()
                .closingDay(10)
                .dueDay(10)
                .build();

        assertThrows(DomainException.class, () -> creditCardServiceAdapter.update(card, profileId));
        verify(creditCardRepositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Should find credit card by id successfully")
    void shouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        CreditCard card = CreditCard.builder().id(id).build();

        when(creditCardRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.of(card));

        CreditCard result = creditCardServiceAdapter.findByIdAndProfileId(id, profileId);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when credit card not found")
    void shouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(creditCardRepositoryPort.findByIdAndProfileId(id, profileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> creditCardServiceAdapter.findByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should delete credit card successfully")
    void shouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(creditCardRepositoryPort.existsByIdAndProfileId(id, profileId)).thenReturn(true);

        creditCardServiceAdapter.delete(id, profileId);

        verify(creditCardRepositoryPort).delete(id);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent credit card")
    void shouldThrowExceptionOnDeleteNotFound() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(creditCardRepositoryPort.existsByIdAndProfileId(id, profileId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> creditCardServiceAdapter.delete(id, profileId));
        verify(creditCardRepositoryPort, never()).delete(any());
    }
}
