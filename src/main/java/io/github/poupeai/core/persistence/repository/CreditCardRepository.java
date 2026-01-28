package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.CreditCardEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCardEntity, UUID> {
    @EntityGraph(attributePaths = {"institution"})
    Optional<CreditCardEntity> findByIdAndProfileUserId(UUID id, UUID profileId);

    @EntityGraph(attributePaths = {"institution"})
    List<CreditCardEntity> findAllByProfileUserId(UUID profileId);

    boolean existsByIdAndProfileUserId(UUID id, UUID profileId);
    boolean existsByNameAndProfileUserId(String name, UUID profileId);
    boolean existsByNameAndProfileUserIdAndIdNot(String name, UUID profileId, UUID id);
}
