package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.external.KeycloakUserPort;
import io.github.poupeai.core.domain.port.messaging.ProfileNotificationProducerPort;
import io.github.poupeai.core.domain.port.persistence.ProfileRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceAdapterTest {

    @Mock
    private ProfileRepositoryPort profileRepositoryPort;

    @Mock
    private ProfileNotificationProducerPort profileNotificationProducerPort;

    @Mock
    private KeycloakUserPort keycloakUserPort;

    @InjectMocks
    private ProfileServiceAdapter profileServiceAdapter;

    @Captor
    private ArgumentCaptor<Profile> profileCaptor;

    @Captor
    private ArgumentCaptor<PoupeAiEvent<?>> eventCaptor;

    @Nested
    @DisplayName("deactivate()")
    class DeactivateTests {

        @Test
        @DisplayName("Should deactivate profile and publish event when profile is active")
        void shouldDeactivateProfileAndPublishEvent() {
            UUID userId = UUID.randomUUID();
            Profile activeProfile = Profile.builder()
                    .userId(userId)
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .deactivated(false)
                    .build();

            when(profileRepositoryPort.findById(userId)).thenReturn(Optional.of(activeProfile));
            when(profileRepositoryPort.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

            profileServiceAdapter.deactivate(userId);

            verify(profileRepositoryPort).save(profileCaptor.capture());
            Profile savedProfile = profileCaptor.getValue();

            assertTrue(savedProfile.isDeactivated());
            assertNotNull(savedProfile.getDeactivationScheduledAt());

            verify(profileNotificationProducerPort).publishDeletionScheduled(eventCaptor.capture());
            PoupeAiEvent<?> event = eventCaptor.getValue();

            assertEquals("PROFILE_DELETION_SCHEDULED", event.getEventType());
            assertEquals("USER_ACTION", event.getTriggerType());
            assertEquals(userId.toString(), event.getRecipient().getUserId());
            assertEquals("test@example.com", event.getRecipient().getEmail());
            assertEquals("John Doe", event.getRecipient().getName());
        }

        @Test
        @DisplayName("Should throw DomainException when profile is already deactivated")
        void shouldThrowExceptionWhenAlreadyDeactivated() {
            UUID userId = UUID.randomUUID();
            Profile deactivatedProfile = Profile.builder()
                    .userId(userId)
                    .deactivated(true)
                    .build();

            when(profileRepositoryPort.findById(userId)).thenReturn(Optional.of(deactivatedProfile));

            assertThrows(DomainException.class, () -> profileServiceAdapter.deactivate(userId));

            verify(profileRepositoryPort, never()).save(any());
            verify(profileNotificationProducerPort, never()).publishDeletionScheduled(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when profile does not exist")
        void shouldThrowExceptionWhenProfileNotFound() {
            UUID userId = UUID.randomUUID();
            when(profileRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> profileServiceAdapter.deactivate(userId));

            verify(profileRepositoryPort, never()).save(any());
            verify(profileNotificationProducerPort, never()).publishDeletionScheduled(any());
        }
    }

    @Nested
    @DisplayName("reactivate()")
    class ReactivateTests {

        @Test
        @DisplayName("Should reactivate profile and clear flags when profile is deactivated")
        void shouldReactivateProfileAndClearFlags() {
            UUID userId = UUID.randomUUID();
            Profile deactivatedProfile = Profile.builder()
                    .userId(userId)
                    .deactivated(true)
                    .deactivationScheduledAt(OffsetDateTime.now().plusDays(30))
                    .build();

            when(profileRepositoryPort.findById(userId)).thenReturn(Optional.of(deactivatedProfile));
            when(profileRepositoryPort.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

            profileServiceAdapter.reactivate(userId);

            verify(profileRepositoryPort).save(profileCaptor.capture());
            Profile savedProfile = profileCaptor.getValue();

            assertFalse(savedProfile.isDeactivated());
            assertNull(savedProfile.getDeactivationScheduledAt());
        }

        @Test
        @DisplayName("Should throw DomainException when profile is already active")
        void shouldThrowExceptionWhenAlreadyActive() {
            UUID userId = UUID.randomUUID();
            Profile activeProfile = Profile.builder()
                    .userId(userId)
                    .deactivated(false)
                    .build();

            when(profileRepositoryPort.findById(userId)).thenReturn(Optional.of(activeProfile));

            assertThrows(DomainException.class, () -> profileServiceAdapter.reactivate(userId));

            verify(profileRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when profile does not exist")
        void shouldThrowExceptionWhenProfileNotFound() {
            UUID userId = UUID.randomUUID();
            when(profileRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> profileServiceAdapter.reactivate(userId));

            verify(profileRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("hardDeleteExpiredProfiles()")
    class HardDeleteTests {

        @Test
        @DisplayName("Should delete from Keycloak and local when both succeed")
        void shouldDeleteAllExpiredProfiles() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            List<Profile> expiredProfiles = List.of(
                    Profile.builder().userId(userId1).email("user1@example.com").build(),
                    Profile.builder().userId(userId2).email("user2@example.com").build());

            when(profileRepositoryPort.findExpiredDeactivatedProfiles(any(OffsetDateTime.class)))
                    .thenReturn(expiredProfiles);
            when(keycloakUserPort.deleteUser(any(UUID.class))).thenReturn(true);

            profileServiceAdapter.hardDeleteExpiredProfiles();

            verify(keycloakUserPort).deleteUser(userId1);
            verify(keycloakUserPort).deleteUser(userId2);
            verify(profileRepositoryPort).delete(userId1);
            verify(profileRepositoryPort).delete(userId2);
        }

        @Test
        @DisplayName("Should handle empty list of expired profiles")
        void shouldHandleEmptyList() {
            when(profileRepositoryPort.findExpiredDeactivatedProfiles(any(OffsetDateTime.class)))
                    .thenReturn(List.of());

            profileServiceAdapter.hardDeleteExpiredProfiles();

            verify(keycloakUserPort, never()).deleteUser(any());
            verify(profileRepositoryPort, never()).delete(any());
        }

        @Test
        @DisplayName("Should not delete local profile when Keycloak deletion fails")
        void shouldNotDeleteLocalWhenKeycloakFails() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            List<Profile> expiredProfiles = List.of(
                    Profile.builder().userId(userId1).email("user1@example.com").build(),
                    Profile.builder().userId(userId2).email("user2@example.com").build());

            when(profileRepositoryPort.findExpiredDeactivatedProfiles(any(OffsetDateTime.class)))
                    .thenReturn(expiredProfiles);
            when(keycloakUserPort.deleteUser(userId1)).thenReturn(false);
            when(keycloakUserPort.deleteUser(userId2)).thenReturn(true);

            assertDoesNotThrow(() -> profileServiceAdapter.hardDeleteExpiredProfiles());

            verify(profileRepositoryPort, never()).delete(userId1);
            verify(profileRepositoryPort).delete(userId2);
        }

        @Test
        @DisplayName("Should continue processing when local deletion fails")
        void shouldContinueOnLocalDeletionError() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            List<Profile> expiredProfiles = List.of(
                    Profile.builder().userId(userId1).email("user1@example.com").build(),
                    Profile.builder().userId(userId2).email("user2@example.com").build());

            when(profileRepositoryPort.findExpiredDeactivatedProfiles(any(OffsetDateTime.class)))
                    .thenReturn(expiredProfiles);
            when(keycloakUserPort.deleteUser(any(UUID.class))).thenReturn(true);
            doThrow(new RuntimeException("DB Error")).when(profileRepositoryPort).delete(userId1);

            assertDoesNotThrow(() -> profileServiceAdapter.hardDeleteExpiredProfiles());

            verify(profileRepositoryPort).delete(userId1);
            verify(profileRepositoryPort).delete(userId2);
        }
    }
}
