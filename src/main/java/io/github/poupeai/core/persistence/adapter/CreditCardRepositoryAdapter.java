package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.persistence.mapper.CreditCardEntityMapper;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InstitutionRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CreditCardRepositoryAdapter implements CreditCardRepositoryPort {
    private final CreditCardRepository creditCardRepository;
    private final CreditCardEntityMapper creditCardMapper;
    private final ProfileRepository profileRepository;
    private final InstitutionRepository institutionRepository;

    @Override
    public CreditCard create(CreditCard creditCard) {
        var entity = creditCardMapper.toEntity(creditCard);

        var profile = profileRepository.getReferenceById(creditCard.getProfileId());
        entity.setProfile(profile);

        if (creditCard.getInstitutionId() != null) {
            var institution = institutionRepository.getReferenceById(creditCard.getInstitutionId());
            entity.setInstitution(institution);
        }

        var savedEntity = creditCardRepository.save(entity);
        return creditCardMapper.toDomain(savedEntity);
    }

    @Override
    public CreditCard update(CreditCard creditCard) {
        var existingEntity = creditCardRepository.findByIdAndProfileUserId(creditCard.getId(), creditCard.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));

        existingEntity.setName(creditCard.getName());
        existingEntity.setCreditLimit(creditCard.getCreditLimit());
        existingEntity.setClosingDay(creditCard.getClosingDay());
        existingEntity.setDueDay(creditCard.getDueDay());

        if (creditCard.getInstitutionId() != null) {
            var institution = institutionRepository.getReferenceById(creditCard.getInstitutionId());
            existingEntity.setInstitution(institution);
        } else {
            existingEntity.setInstitution(null);
        }

        var savedEntity = creditCardRepository.save(existingEntity);
        return creditCardMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CreditCard> findByIdAndProfileId(UUID id, UUID profileId) {
        return creditCardRepository.findByIdAndProfileUserId(id, profileId)
                .map(creditCardMapper::toDomain);
    }

    @Override
    public List<CreditCard> findAllByProfileId(UUID profileId) {
        var entities = creditCardRepository.findAllByProfileUserId(profileId);
        return creditCardMapper.toDomainList(entities);
    }

    @Override
    public void delete(UUID id) {
        creditCardRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndProfileId(UUID id, UUID profileId) {
        return creditCardRepository.existsByIdAndProfileUserId(id, profileId);
    }

    @Override
    public boolean isNameTaken(String name, UUID profileId, UUID excludeId) {
        if (excludeId == null) {
            return creditCardRepository.existsByNameAndProfileUserId(name, profileId);
        }
        return creditCardRepository.existsByNameAndProfileUserIdAndIdNot(name, profileId, excludeId);
    }
}
