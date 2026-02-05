package io.github.poupeai.core.web.controller.profile;

import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.GetProfilePort;
import io.github.poupeai.core.domain.port.business.ProfileServicePort;
import io.github.poupeai.core.web.mapper.profile.ProfileControllerMapper;
import io.github.poupeai.core.web.dto.profile.ProfileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @InjectMocks
    private ProfileController profileController;

    @Mock
    private GetProfilePort getProfilePort;

    @Mock
    private ProfileServicePort profileServicePort;

    @Mock
    private ProfileControllerMapper mapper;

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfileTests {

        @Test
    @DisplayName("Should successfully return the profile when the UUID is exists (Happy Path).")
        void getMyProfileShouldReturnProfileWhenUserExists() {
            String validUserId = "550e8400-e29b-41d4-a716-446655440000";
            UUID uuid = UUID.fromString(validUserId);

            Profile mockProfile = new Profile();
            ProfileResponse mockResponse = new ProfileResponse();

            when(getProfilePort.execute(uuid)).thenReturn(mockProfile);
            when(mapper.toResponse(mockProfile)).thenReturn(mockResponse);

            ResponseEntity<ProfileResponse> response = profileController.getMyProfile(validUserId);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(mockResponse, response.getBody());

            verify(getProfilePort, times(1)).execute(uuid);
            verify(mapper, times(1)).toResponse(mockProfile);
        }

        @Test
    @DisplayName("Should thrown ResourceNotFoundException when the profile is not found on the entry port")
        void getMyProfileShouldThrowNotFoundWhenProfileDoesNotExist() {
            String validUserId = "550e8400-e29b-41d4-a716-446655440000";
            UUID uuid = UUID.fromString(validUserId);

            when(getProfilePort.execute(uuid)).thenThrow(new ResourceNotFoundException("Perfil não encontrado"));

            assertThrows(ResourceNotFoundException.class, () -> {
                profileController.getMyProfile(validUserId);
            });

            verify(getProfilePort).execute(uuid);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("deactivateProfile()")
    class DeactivateProfileTests {

        @Test
        @DisplayName("Should return 200 when deactivation is successful")
        void shouldReturn200WhenDeactivationSuccessful() {
            String validUserId = "550e8400-e29b-41d4-a716-446655440000";
            UUID uuid = UUID.fromString(validUserId);

            doNothing().when(profileServicePort).deactivate(uuid);

            ResponseEntity<Map<String, String>> response = profileController.deactivateProfile(validUserId);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().containsKey("detail"));

            verify(profileServicePort, times(1)).deactivate(uuid);
        }

        @Test
        @DisplayName("Should throw DomainException when profile is already deactivated")
        void shouldThrowDomainExceptionWhenAlreadyDeactivated() {
            String validUserId = "550e8400-e29b-41d4-a716-446655440000";
            UUID uuid = UUID.fromString(validUserId);

            doThrow(new DomainException("Perfil já está desativado"))
                    .when(profileServicePort).deactivate(uuid);

            assertThrows(DomainException.class, () -> {
                profileController.deactivateProfile(validUserId);
            });

            verify(profileServicePort).deactivate(uuid);
        }
    }

    @Nested
    @DisplayName("reactivateProfile()")
    class ReactivateProfileTests {

        @Test
        @DisplayName("Should return 200 when reactivation is successful")
        void shouldReturn200WhenReactivationSuccessful() {
            String validUserId = "550e8400-e29b-41d4-a716-446655440000";
            UUID uuid = UUID.fromString(validUserId);

            doNothing().when(profileServicePort).reactivate(uuid);

            ResponseEntity<Map<String, String>> response = profileController.reactivateProfile(validUserId);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().containsKey("detail"));

            verify(profileServicePort, times(1)).reactivate(uuid);
        }

        @Test
        @DisplayName("Should throw DomainException when profile is already active")
        void shouldThrowDomainExceptionWhenAlreadyActive() {
            String validUserId = "550e8400-e29b-41d4-a716-446655440000";
            UUID uuid = UUID.fromString(validUserId);

            doThrow(new DomainException("Perfil já está ativo"))
                    .when(profileServicePort).reactivate(uuid);

            assertThrows(DomainException.class, () -> {
                profileController.reactivateProfile(validUserId);
            });

            verify(profileServicePort).reactivate(uuid);
        }
    }
}
