package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.GoalDeposit;
import io.github.poupeai.core.persistence.entity.GoalDepositEntity;
import io.github.poupeai.core.persistence.entity.GoalEntity;
import io.github.poupeai.core.persistence.mapper.GoalDepositEntityMapper;
import io.github.poupeai.core.persistence.repository.GoalDepositRepository;
import io.github.poupeai.core.persistence.repository.GoalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalDepositRepositoryAdapterTest {

    @InjectMocks
    private GoalDepositRepositoryAdapter adapter;

    @Mock
    private GoalDepositRepository goalDepositRepository;

    @Mock
    private GoalDepositEntityMapper goalDepositMapper;

    @Mock
    private GoalRepository goalRepository;

    @Test
    @DisplayName("Should persist deposit and link goal when data is valid")
    void createShouldPersistWhenDataIsValid() {
        UUID goalId = UUID.randomUUID();
        GoalDeposit domain = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(500))
                .depositDate(LocalDate.now())
                .build();

        GoalDepositEntity entity = new GoalDepositEntity();
        GoalEntity goalProxy = new GoalEntity();

        when(goalDepositMapper.toEntity(domain)).thenReturn(entity);
        when(goalRepository.getReferenceById(goalId)).thenReturn(goalProxy);
        when(goalDepositRepository.save(entity)).thenReturn(entity);
        when(goalDepositMapper.toDomain(entity)).thenReturn(domain);

        GoalDeposit result = adapter.create(domain);

        assertNotNull(result);
        verify(goalDepositRepository).save(entity);
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("Should return list of deposits mapped to domain")
    void findAllByGoalIdShouldReturnList() {
        UUID goalId = UUID.randomUUID();
        List<GoalDepositEntity> entities = List.of(
            GoalDepositEntity.builder().depositAmount(BigDecimal.valueOf(100)).build(),
            GoalDepositEntity.builder().depositAmount(BigDecimal.valueOf(200)).build()
        );
        List<GoalDeposit> domains = List.of(
            GoalDeposit.builder().depositAmount(BigDecimal.valueOf(100)).build(),
            GoalDeposit.builder().depositAmount(BigDecimal.valueOf(200)).build()
        );

        when(goalDepositRepository.findAllByGoal_Id(goalId)).thenReturn(entities);
        when(goalDepositMapper.toDomainList(entities)).thenReturn(domains);

        List<GoalDeposit> result = adapter.findAllByGoalId(goalId);

        assertEquals(2, result.size());
        verify(goalDepositRepository).findAllByGoal_Id(goalId);
    }

    @Test
    @DisplayName("Should return empty list when goal has no deposits")
    void findAllByGoalIdShouldReturnEmptyListWhenNoDeposits() {
        UUID goalId = UUID.randomUUID();
        when(goalDepositRepository.findAllByGoal_Id(goalId)).thenReturn(List.of());
        when(goalDepositMapper.toDomainList(List.of())).thenReturn(List.of());

        List<GoalDeposit> result = adapter.findAllByGoalId(goalId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return deposit when found by ID and Goal ID")
    void findByIdAndGoalIdShouldReturnDepositWhenFound() {
        UUID depositId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        GoalDepositEntity entity = new GoalDepositEntity();
        GoalDeposit domain = new GoalDeposit();

        when(goalDepositRepository.findByIdAndGoal_Id(depositId, goalId)).thenReturn(Optional.of(entity));
        when(goalDepositMapper.toDomain(entity)).thenReturn(domain);

        Optional<GoalDeposit> result = adapter.findByIdAndGoalId(depositId, goalId);

        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should return empty optional when deposit is not found by ID and Goal ID")
    void findByIdAndGoalIdShouldReturnEmptyWhenNotFound() {
        UUID depositId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();
        when(goalDepositRepository.findByIdAndGoal_Id(depositId, goalId)).thenReturn(Optional.empty());

        Optional<GoalDeposit> result = adapter.findByIdAndGoalId(depositId, goalId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should call delete by ID in repository")
    void deleteShouldCallRepository() {
        UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(goalDepositRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should create deposit with specific date")
    void createShouldSetSpecificDate() {
        UUID goalId = UUID.randomUUID();
        LocalDate specificDate = LocalDate.of(2025, 6, 15);
        GoalDeposit domain = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(1000))
                .depositDate(specificDate)
                .build();

        GoalDepositEntity entity = GoalDepositEntity.builder()
                .depositDate(specificDate)
                .build();
        GoalEntity goalProxy = new GoalEntity();

        when(goalDepositMapper.toEntity(domain)).thenReturn(entity);
        when(goalRepository.getReferenceById(goalId)).thenReturn(goalProxy);
        when(goalDepositRepository.save(entity)).thenReturn(entity);
        when(goalDepositMapper.toDomain(entity)).thenReturn(domain);

        GoalDeposit result = adapter.create(domain);

        assertNotNull(result);
        assertEquals(specificDate, entity.getDepositDate());
    }

    @Test
    @DisplayName("Should create multiple deposits for same goal")
    void createShouldAllowMultipleDepositsForSameGoal() {
        UUID goalId = UUID.randomUUID();
        GoalDeposit deposit1 = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(100))
                .depositDate(LocalDate.now())
                .build();
        GoalDeposit deposit2 = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(BigDecimal.valueOf(200))
                .depositDate(LocalDate.now())
                .build();

        GoalDepositEntity entity1 = new GoalDepositEntity();
        GoalDepositEntity entity2 = new GoalDepositEntity();
        GoalEntity goalProxy = new GoalEntity();

        when(goalDepositMapper.toEntity(deposit1)).thenReturn(entity1);
        when(goalDepositMapper.toEntity(deposit2)).thenReturn(entity2);
        when(goalRepository.getReferenceById(goalId)).thenReturn(goalProxy);
        when(goalDepositRepository.save(entity1)).thenReturn(entity1);
        when(goalDepositRepository.save(entity2)).thenReturn(entity2);
        when(goalDepositMapper.toDomain(entity1)).thenReturn(deposit1);
        when(goalDepositMapper.toDomain(entity2)).thenReturn(deposit2);

        GoalDeposit result1 = adapter.create(deposit1);
        GoalDeposit result2 = adapter.create(deposit2);

        assertNotNull(result1);
        assertNotNull(result2);
        verify(goalDepositRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should create deposit with large amount")
    void createShouldHandleLargeAmount() {
        UUID goalId = UUID.randomUUID();
        BigDecimal largeAmount = BigDecimal.valueOf(999999.99);
        GoalDeposit domain = GoalDeposit.builder()
                .goalId(goalId)
                .depositAmount(largeAmount)
                .depositDate(LocalDate.now())
                .build();

        GoalDepositEntity entity = GoalDepositEntity.builder()
                .depositAmount(largeAmount)
                .build();
        GoalEntity goalProxy = new GoalEntity();

        when(goalDepositMapper.toEntity(domain)).thenReturn(entity);
        when(goalRepository.getReferenceById(goalId)).thenReturn(goalProxy);
        when(goalDepositRepository.save(entity)).thenReturn(entity);
        when(goalDepositMapper.toDomain(entity)).thenReturn(domain);

        GoalDeposit result = adapter.create(domain);

        assertNotNull(result);
        assertEquals(largeAmount, entity.getDepositAmount());
    }

    @Test
    @DisplayName("Should find deposits ordered by repository default order")
    void findAllShouldRespectRepositoryOrder() {
        UUID goalId = UUID.randomUUID();
        List<GoalDepositEntity> entities = List.of(
            GoalDepositEntity.builder().id(UUID.randomUUID()).build(),
            GoalDepositEntity.builder().id(UUID.randomUUID()).build(),
            GoalDepositEntity.builder().id(UUID.randomUUID()).build()
        );
        List<GoalDeposit> domains = List.of(
            GoalDeposit.builder().id(entities.get(0).getId()).build(),
            GoalDeposit.builder().id(entities.get(1).getId()).build(),
            GoalDeposit.builder().id(entities.get(2).getId()).build()
        );

        when(goalDepositRepository.findAllByGoal_Id(goalId)).thenReturn(entities);
        when(goalDepositMapper.toDomainList(entities)).thenReturn(domains);

        List<GoalDeposit> result = adapter.findAllByGoalId(goalId);

        assertEquals(3, result.size());
        assertEquals(domains.get(0).getId(), result.get(0).getId());
        assertEquals(domains.get(1).getId(), result.get(1).getId());
        assertEquals(domains.get(2).getId(), result.get(2).getId());
    }
}
