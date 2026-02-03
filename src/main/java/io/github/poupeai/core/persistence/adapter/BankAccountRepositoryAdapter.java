package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.persistence.entity.BankAccountEntity;
import io.github.poupeai.core.persistence.mapper.BankAccountEntityMapper;
import io.github.poupeai.core.persistence.repository.BankAccountRepository;
import io.github.poupeai.core.persistence.repository.InstitutionRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BankAccountRepositoryAdapter implements BankAccountRepositoryPort {
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountEntityMapper bankAccountMapper;
    private final ProfileRepository profileRepository;
    private final InstitutionRepository institutionRepository;

    @Override
    public BankAccount create(BankAccount bankAccount) {
        var entity = bankAccountMapper.toEntity(bankAccount);

        var profile = profileRepository.getReferenceById(bankAccount.getProfileId());
        entity.setProfile(profile);

        setRelationships(entity, bankAccount);

        return bankAccountMapper.toDomain(bankAccountRepository.save(entity));
    }

    @Override
    public BankAccount update(BankAccount bankAccount) {
        var existingEntity = bankAccountRepository.findByIdAndProfileUserId(bankAccount.getId(), bankAccount.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada."));

        existingEntity.setName(bankAccount.getName());
        existingEntity.setDescription(bankAccount.getDescription());
        existingEntity.setIsDefault(bankAccount.getIsDefault());

        setRelationships(existingEntity, bankAccount);

        return bankAccountMapper.toDomain(bankAccountRepository.save(existingEntity));
    }

    @Override
    public Optional<BankAccount> findByIdAndProfileId(UUID id, UUID profileId) {
        return bankAccountRepository.findByIdAndProfileUserId(id, profileId)
                .map(bankAccountMapper::toDomain);
    }

    @Override
    public List<BankAccount> findAllByProfileId(UUID profileId) {
        var entities = bankAccountRepository.findAllByProfileUserId(profileId);
        return bankAccountMapper.toDomainList(entities);
    }

    @Override
    public void delete(UUID id) {
        bankAccountRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return bankAccountRepository.existsByIdAndProfileUserId(id, profileId);
    }

    @Override
    public boolean isNameTaken(String name, UUID profileId, UUID excludeId) {
        if (excludeId == null) {
            return bankAccountRepository.existsByNameAndProfileUserId(name, profileId);
        }
        return bankAccountRepository.existsByNameAndProfileUserIdAndIdNot(name, profileId, excludeId);
    }

    @Override
    public Optional<BankAccount> findDefaultByProfileId(UUID profileId) {
        return bankAccountRepository.findByProfileUserIdAndIsDefaultTrue(profileId)
                .map(bankAccountMapper::toDomain);
    }

    @Override
    public long countByProfileId(UUID profileId) {
        return bankAccountRepository.countByProfileUserId(profileId);
    }

    @Override
    @Transactional
    public void clearDefaultByProfileId(UUID profileId) {
        bankAccountRepository.clearDefaultByProfileUserId(profileId);
    }

    private void setRelationships(BankAccountEntity entity, BankAccount domain) {
        if (domain.getProfileId() != null) {
            entity.setProfile(profileRepository.getReferenceById(domain.getProfileId()));
        }

        if (domain.getInstitution() != null && domain.getInstitution().getId() != null) {
            entity.setInstitution(institutionRepository.getReferenceById(domain.getInstitution().getId()));
        } else {
            entity.setInstitution(null);
        }
    }
}
