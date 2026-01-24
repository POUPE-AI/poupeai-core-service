package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.IngestionJob;
import io.github.poupeai.core.persistence.entity.IngestionJobEntity;
import io.github.poupeai.core.persistence.mapper.IngestionJobEntityMapper;
import io.github.poupeai.core.persistence.repository.IngestionJobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionJobRepositoryAdapterTest {

    @InjectMocks
    private IngestionJobRepositoryAdapter adapter;

    @Mock
    private IngestionJobRepository jpaRepository;

    @Mock
    private IngestionJobEntityMapper mapper;

    @Test
    @DisplayName("Should save ingestion job")
    void shouldSaveIngestionJob() {
        IngestionJob domain = IngestionJob.builder().id(UUID.randomUUID()).build();
        IngestionJobEntity entity = new IngestionJobEntity();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        IngestionJob result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(domain, result);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("Should find ingestion job by id")
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        IngestionJobEntity entity = new IngestionJobEntity();
        IngestionJob domain = IngestionJob.builder().id(id).build();

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<IngestionJob> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find all ingestion jobs by profile id")
    void shouldFindAllByProfileId() {
        UUID profileId = UUID.randomUUID();
        List<IngestionJobEntity> entities = List.of(new IngestionJobEntity(), new IngestionJobEntity());
        IngestionJob domain1 = IngestionJob.builder().id(UUID.randomUUID()).profileId(profileId).build();
        IngestionJob domain2 = IngestionJob.builder().id(UUID.randomUUID()).profileId(profileId).build();

        when(jpaRepository.findAllByProfileIdOrderByCreatedAtDesc(profileId)).thenReturn(entities);
        when(mapper.toDomain(any(IngestionJobEntity.class))).thenReturn(domain1, domain2);

        List<IngestionJob> result = adapter.findAllByProfileId(profileId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(j -> domain1.getId().equals(j.getId())));
        assertTrue(result.stream().anyMatch(j -> domain2.getId().equals(j.getId())));

        ArgumentCaptor<IngestionJobEntity> entityCaptor = ArgumentCaptor.forClass(IngestionJobEntity.class);
        verify(mapper, times(2)).toDomain(entityCaptor.capture());
        assertEquals(2, entityCaptor.getAllValues().size());
    }
}
