package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.domain.model.InstitutionType;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceAdapterTest {

    @Mock
    private InstitutionRepositoryPort institutionRepositoryPort;

    @InjectMocks
    private InstitutionServiceAdapter institutionAdapter;

    @Test
    @DisplayName("Should return all institutions successfully")
    void shouldReturnAllInstitutionsSuccessfully() {
        List<Institution> institutions = List.of(
            Institution.builder()
                .id(1L)
                .name("Nubank")
                .mainColorHex("#820AD1")
                .logoName("nubank")
                .type(InstitutionType.BOTH)
                .build(),
            Institution.builder()
                .id(2L)
                .name("Itaú")
                .mainColorHex("#EC7000")
                .logoName("itau")
                .type(InstitutionType.BOTH)
                .build()
        );

        when(institutionRepositoryPort.findAll()).thenReturn(institutions);

        List<Institution> result = institutionAdapter.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Nubank", result.get(0).getName());
        assertEquals("Itaú", result.get(1).getName());
        verify(institutionRepositoryPort).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no institutions exist")
    void shouldReturnEmptyListWhenNoInstitutionsExist() {
        when(institutionRepositoryPort.findAll()).thenReturn(List.of());

        List<Institution> result = institutionAdapter.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(institutionRepositoryPort).findAll();
    }
}
