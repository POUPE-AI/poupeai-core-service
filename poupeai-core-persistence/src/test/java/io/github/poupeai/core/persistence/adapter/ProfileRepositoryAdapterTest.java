package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.persistence.entity.ProfileEntity;
import io.github.poupeai.core.persistence.mapper.ProfileEntityMapper;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileRepositoryAdapterTest {

    @InjectMocks
    private ProfileRepositoryAdapter adapter;

    @Mock
    private ProfileRepository repository;

    @Mock
    private ProfileEntityMapper mapper;

    @Test
    @DisplayName("Should save a profile by converting it to an entity and returning the domain")
    void saveShouldPersistAndReturnDomain() {
        Profile domainProfile = new Profile();
        ProfileEntity entityProfile = new ProfileEntity();
        ProfileEntity savedEntity = new ProfileEntity();
        Profile savedDomain = new Profile();

        when(mapper.toEntity(domainProfile)).thenReturn(entityProfile);
        when(repository.save(entityProfile)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        Profile result = adapter.save(domainProfile);

        assertNotNull(result);
        assertEquals(savedDomain, result);

        verify(mapper).toEntity(domainProfile);
        verify(repository).save(entityProfile);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Should return an Optional with a profile when found by ID")
    void findByIdShouldReturnProfileWhenFound() {
        UUID id = UUID.randomUUID();
        ProfileEntity entity = new ProfileEntity();
        Profile domain = new Profile();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Profile> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Should return an empty Optional when not found by ID")
    void findByIdShouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Profile> result = adapter.findById(id);

        assertTrue(result.isEmpty());
        verify(repository).findById(id);

        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Should return the Optional with profile when found by Email")
    void findByEmailShouldReturnProfileWhenFound() {
        String email = "teste@email.com";
        ProfileEntity entity = new ProfileEntity();
        Profile domain = new Profile();

        when(repository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Profile> result = adapter.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
        verify(repository).findByEmail(email);
    }

    @Test
    @DisplayName("Should return an empty Optional when not found by Email")
    void findByEmailShouldReturnEmptyWhenNotFound() {
        String email = "inexistente@email.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<Profile> result = adapter.findByEmail(email);

        assertTrue(result.isEmpty());
        verify(repository).findByEmail(email);
        verifyNoInteractions(mapper);
    }
}
