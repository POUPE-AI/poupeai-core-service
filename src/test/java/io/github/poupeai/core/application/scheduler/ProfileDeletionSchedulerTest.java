package io.github.poupeai.core.application.scheduler;

import io.github.poupeai.core.domain.port.business.ProfileServicePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileDeletionSchedulerTest {

    @Mock
    private ProfileServicePort profileServicePort;

    @InjectMocks
    private ProfileDeletionScheduler profileDeletionScheduler;

    @Test
    @DisplayName("Should call hardDeleteExpiredProfiles from service port")
    void shouldCallHardDeleteExpiredProfiles() {
        profileDeletionScheduler.processExpiredProfiles();

        verify(profileServicePort, times(1)).hardDeleteExpiredProfiles();
    }

    @Test
    @DisplayName("Should handle exceptions gracefully without rethrowing")
    void shouldHandleExceptionsGracefully() {
        doThrow(new RuntimeException("Test exception")).when(profileServicePort).hardDeleteExpiredProfiles();

        profileDeletionScheduler.processExpiredProfiles();

        verify(profileServicePort, times(1)).hardDeleteExpiredProfiles();
    }
}
