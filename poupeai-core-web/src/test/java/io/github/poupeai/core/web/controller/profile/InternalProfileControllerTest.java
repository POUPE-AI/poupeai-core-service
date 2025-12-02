package io.github.poupeai.core.web.controller.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.CreateOrUpdateProfilePort;
import io.github.poupeai.core.web.dto.profile.ProfileRequest;
import io.github.poupeai.core.web.dto.profile.ProfileResponse;
import io.github.poupeai.core.web.mapper.profile.ProfileControllerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class InternalProfileControllerTest {
    private MockMvc mockMvc;

    @Mock
    private CreateOrUpdateProfilePort createOrUpdateProfilePort;

    @Mock
    private ProfileControllerMapper mapper;

    private ObjectMapper objectMapper;

    @InjectMocks
    private InternalProfileController internalProfileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(internalProfileController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should sync profile successfully")
    void shouldSyncProfileSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        ProfileRequest request = new ProfileRequest(id, "test@email.com", "John", "Doe");
        Profile domain = Profile.builder().userId(id).build();
        ProfileResponse response = new ProfileResponse(id, "test@email.com", "John", "Doe", false);

        when(mapper.toDomain(any())).thenReturn(domain);
        when(createOrUpdateProfilePort.execute(any())).thenReturn(domain);
        when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/internal/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }
}
