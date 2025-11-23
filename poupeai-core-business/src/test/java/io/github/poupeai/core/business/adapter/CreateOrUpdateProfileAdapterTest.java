package io.github.poupeai.core.business.adapter;

import io.github.poupeai.domain.model.Profile;
import io.github.poupeai.domain.port.persistence.ProfileRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateOrUpdateProfileAdapterTest {
    @Mock
    private ProfileRepositoryPort profileRepositoryPort;

    @InjectMocks
    private CreateOrUpdateProfileAdapter createOrUpdateProfileAdapter;

    @Test
    @DisplayName("Should create new profile when profile does not exist")
    void shouldCreateNewProfileWhenProfileDoesNotExist() {
        UUID userId = UUID.randomUUID();
        Profile input = Profile.builder()
                .userId(userId)
                .email("test@email.com")
                .build();

        when(profileRepositoryPort.findById(userId)).thenReturn(Optional.empty());
        when(profileRepositoryPort.save(any(Profile.class))).thenReturn(input);

        Profile result = createOrUpdateProfileAdapter.execute(input);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(profileRepositoryPort).save(input);

    }

    @Test
    @DisplayName("Should update profile when exists")
    void shouldUpdateProfileWhenExists() {
        UUID userId = UUID.randomUUID();

        Profile existing = Profile.builder()
                .userId(userId)
                .firstName("Old")
                .build();

        Profile input = Profile.builder()
                .userId(userId)
                .firstName("New")
                .build();

        when(profileRepositoryPort.findById(userId)).thenReturn(Optional.of(existing));
        when(profileRepositoryPort.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile result = createOrUpdateProfileAdapter.execute(input);

        assertEquals("New", result.getFirstName());
        verify(profileRepositoryPort).save(existing);
    }
}
