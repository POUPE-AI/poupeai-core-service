package io.github.poupeai.core.persistence.repository;

import io.github.poupeai.core.persistence.entity.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, UUID> {
    Optional<BankAccountEntity> findByIdAndProfileUserId(UUID id, UUID profileId);
    List<BankAccountEntity> findAllByProfileUserId(UUID profileId);
    boolean existsByIdAndProfileUserId(UUID id, UUID profileId);
    boolean existsByNameAndProfileUserId(String name, UUID profileId);
    boolean existsByNameAndProfileUserIdAndIdNot(String name, UUID profileId, UUID id);
    Optional<BankAccountEntity> findByProfileUserIdAndIsDefaultTrue(UUID profileId);
    long countByProfileUserId(UUID profileId);

    @Modifying
    @Query("UPDATE BankAccountEntity b SET b.isDefault = false WHERE b.profile.userId = :profileId")
    void clearDefaultByProfileUserId(@Param("profileId") UUID profileId);
}
