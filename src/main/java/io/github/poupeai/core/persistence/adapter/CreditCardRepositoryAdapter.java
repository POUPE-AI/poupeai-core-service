package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.persistence.entity.CreditCardEntity;
import io.github.poupeai.core.persistence.mapper.CreditCardEntityMapper;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InstitutionRepository;
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
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
    private final InvoiceRepository invoiceRepository;

    @Override
    public CreditCard create(CreditCard creditCard) {
        var entity = creditCardMapper.toEntity(creditCard);

        var profile = profileRepository.getReferenceById(creditCard.getProfileId());
        entity.setProfile(profile);

        if (creditCard.getInstitution() != null && creditCard.getInstitution().getId() != null) {
            var institution = institutionRepository.getReferenceById(creditCard.getInstitution().getId());
            entity.setInstitution(institution);
        }

        var savedEntity = creditCardRepository.save(entity);
        return toDomainWithUsedLimit(savedEntity);
    }

    @Override
    public CreditCard update(CreditCard creditCard) {
        var existingEntity = creditCardRepository.findByIdAndProfileUserId(creditCard.getId(), creditCard.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));

        existingEntity.setName(creditCard.getName());
        existingEntity.setCreditLimit(creditCard.getCreditLimit());
        existingEntity.setClosingDay(creditCard.getClosingDay());
        existingEntity.setDueDay(creditCard.getDueDay());

        if (creditCard.getInstitution() != null && creditCard.getInstitution().getId() != null) {
            var institution = institutionRepository.getReferenceById(creditCard.getInstitution().getId());
            existingEntity.setInstitution(institution);
        } else {
            existingEntity.setInstitution(null);
        }

        var savedEntity = creditCardRepository.save(existingEntity);
        return toDomainWithUsedLimit(savedEntity);
    }

    @Override
    public Optional<CreditCard> findByIdAndProfileId(UUID id, UUID profileId) {
        return creditCardRepository.findByIdAndProfileUserId(id, profileId)
                .map(this::toDomainWithUsedLimit);
    }

    @Override
    public List<CreditCard> findAllByProfileId(UUID profileId) {
        var entities = creditCardRepository.findAllByProfileUserId(profileId);
        return entities.stream()
                .map(this::toDomainWithUsedLimit)
                .toList();
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

    private CreditCard toDomainWithUsedLimit(CreditCardEntity entity) {
        var creditCard = creditCardMapper.toDomain(entity);
        var usedLimit = invoiceRepository.calculateUsedCreditLimit(entity.getId());
        creditCard.setUsedCreditLimit(usedLimit);
        return creditCard;
    }
}
