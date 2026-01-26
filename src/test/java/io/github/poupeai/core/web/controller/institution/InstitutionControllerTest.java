package io.github.poupeai.core.web.controller.institution;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.domain.model.InstitutionType;
import io.github.poupeai.core.domain.port.business.InstitutionServicePort;
import io.github.poupeai.core.web.dto.institution.InstitutionResponse;
import io.github.poupeai.core.web.mapper.institution.InstitutionControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionControllerTest {

    @Mock
    private InstitutionServicePort institutionServicePort;

    @Mock
    private InstitutionControllerMapper institutionMapper;

    @InjectMocks
    private InstitutionController institutionController;

    @Test
    @DisplayName("Should get institutions successfully")
    void shouldGetInstitutionsSuccessfully() {
        List<Institution> institutions = List.of(
            Institution.builder()
                .id(1L)
                .name("Nubank")
                .mainColorHex("#820AD1")
                .logoName("nubank")
                .type(InstitutionType.BOTH)
                .build()
        );

        List<InstitutionResponse> responses = List.of(
            InstitutionResponse.builder()
                .id(1L)
                .name("Nubank")
                .mainColorHex("#820AD1")
                .logoName("nubank")
                .type(InstitutionType.BOTH)
                .build()
        );

        when(institutionServicePort.findAll()).thenReturn(institutions);
        when(institutionMapper.toResponseList(institutions)).thenReturn(responses);

        ResponseEntity<List<InstitutionResponse>> result = institutionController.getInstitutions();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("Nubank", result.getBody().get(0).getName());
        verify(institutionServicePort).findAll();
        verify(institutionMapper).toResponseList(institutions);
    }

    @Test
    @DisplayName("Should return empty list when no institutions exist")
    void shouldReturnEmptyListWhenNoInstitutionsExist() {
        when(institutionServicePort.findAll()).thenReturn(List.of());
        when(institutionMapper.toResponseList(List.of())).thenReturn(List.of());

        ResponseEntity<List<InstitutionResponse>> result = institutionController.getInstitutions();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(institutionServicePort).findAll();
    }
}
