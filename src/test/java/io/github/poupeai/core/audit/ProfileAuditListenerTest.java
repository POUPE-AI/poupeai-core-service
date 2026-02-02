package io.github.poupeai.core.audit;

import io.github.poupeai.core.persistence.entity.ProfileEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileAuditListenerTest {

    private ProfileAuditListener listener;

    @Mock
    private ApplicationContext applicationContext;

    private MockedStatic<ApplicationContextProvider> applicationContextProviderMock;

    @BeforeEach
    void setUp() {
        listener = new ProfileAuditListener();
        applicationContextProviderMock = Mockito.mockStatic(ApplicationContextProvider.class);
        applicationContextProviderMock.when(ApplicationContextProvider::getApplicationContext)
                .thenReturn(applicationContext);
    }

    @AfterEach
    void tearDown() {
        applicationContextProviderMock.close();
        AuditContext.clear();
    }

    @Test
    @DisplayName("Should publish audit event on profile creation")
    void postPersist_shouldPublishEventForCreate() {
        UUID userId = UUID.randomUUID();
        ProfileEntity entity = createProfileEntity(userId);

        listener.postPersist(entity);

        ArgumentCaptor<ProfileAuditEvent> captor = ArgumentCaptor.forClass(ProfileAuditEvent.class);
        verify(applicationContext).publishEvent(captor.capture());

        ProfileAuditEvent event = captor.getValue();
        assertEquals("CREATE", event.actionType());
        assertEquals("Profile", event.entityType());
        assertEquals(userId, event.profileId());
        assertNull(event.changes());
    }

    @Test
    @DisplayName("Should publish audit event on profile deletion")
    void postRemove_shouldPublishEventForDelete() {
        UUID userId = UUID.randomUUID();
        ProfileEntity entity = createProfileEntity(userId);

        listener.postRemove(entity);

        ArgumentCaptor<ProfileAuditEvent> captor = ArgumentCaptor.forClass(ProfileAuditEvent.class);
        verify(applicationContext).publishEvent(captor.capture());

        ProfileAuditEvent event = captor.getValue();
        assertEquals("DELETE", event.actionType());
        assertEquals("Profile", event.entityType());
        assertEquals(userId, event.profileId());
    }

    @Test
    @DisplayName("Should capture context in audit event")
    void postPersist_shouldCaptureAuditContext() {
        UUID userId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        String sourceIp = "192.168.1.1";

        AuditContext.setSourceIp(sourceIp);
        AuditContext.setCorrelationId(correlationId);

        ProfileEntity entity = createProfileEntity(userId);

        listener.postPersist(entity);

        ArgumentCaptor<ProfileAuditEvent> captor = ArgumentCaptor.forClass(ProfileAuditEvent.class);
        verify(applicationContext).publishEvent(captor.capture());

        ProfileAuditEvent event = captor.getValue();
        assertEquals(sourceIp, event.sourceIp());
        assertEquals(correlationId, event.correlationId());
    }

    @Test
    @DisplayName("Should detect changes on profile update")
    void postUpdate_shouldDetectChanges() {
        UUID userId = UUID.randomUUID();
        ProfileEntity entity = ProfileEntity.builder()
                .userId(userId)
                .email("old@example.com")
                .firstName("OldFirst")
                .lastName("OldLast")
                .deactivated(false)
                .build();

        listener.postLoad(entity);

        entity.setEmail("new@example.com");
        entity.setFirstName("NewFirst");

        listener.postUpdate(entity);

        ArgumentCaptor<ProfileAuditEvent> captor = ArgumentCaptor.forClass(ProfileAuditEvent.class);
        verify(applicationContext).publishEvent(captor.capture());

        ProfileAuditEvent event = captor.getValue();
        assertEquals("UPDATE", event.actionType());
        assertNotNull(event.changes());
        assertTrue(event.changes().containsKey("email"));
        assertTrue(event.changes().containsKey("firstName"));
        assertFalse(event.changes().containsKey("lastName"));
    }

    @Test
    @DisplayName("Should not fail when context is unavailable")
    void postPersist_shouldNotFailWhenContextUnavailable() {
        applicationContextProviderMock.when(ApplicationContextProvider::getApplicationContext)
                .thenReturn(null);

        UUID userId = UUID.randomUUID();
        ProfileEntity entity = createProfileEntity(userId);

        assertDoesNotThrow(() -> listener.postPersist(entity));
        verify(applicationContext, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not fail when publishing throws exception")
    void postPersist_shouldNotFailWhenPublishingThrowsException() {
        UUID userId = UUID.randomUUID();
        ProfileEntity entity = createProfileEntity(userId);

        doThrow(new RuntimeException("Event publishing error")).when(applicationContext).publishEvent(any());

        assertDoesNotThrow(() -> listener.postPersist(entity));
    }

    @Test
    @DisplayName("Should skip update event when no changes detected")
    void postUpdate_shouldSkipWhenNoChanges() {
        UUID userId = UUID.randomUUID();
        ProfileEntity entity = createProfileEntity(userId);

        listener.postLoad(entity);

        listener.postUpdate(entity);

        verify(applicationContext, never()).publishEvent(any());
    }

    private ProfileEntity createProfileEntity(UUID userId) {
        return ProfileEntity.builder()
                .userId(userId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .deactivated(false)
                .build();
    }
}
