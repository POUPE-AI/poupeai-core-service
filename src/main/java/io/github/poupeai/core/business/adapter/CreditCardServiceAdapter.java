package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.audit.Log;
import io.github.poupeai.core.domain.exception.DomainException;
import io.github.poupeai.core.domain.exception.ResourceAlreadyExistsException;
import io.github.poupeai.core.domain.exception.ResourceNotFoundException;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.port.business.CreditCardServicePort;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InstitutionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardServiceAdapter implements CreditCardServicePort {
    private final CreditCardRepositoryPort creditCardRepositoryPort;
    private final InstitutionRepositoryPort institutionRepositoryPort;

    @Override
    @Transactional
    public CreditCard create(CreditCard creditCard) {
        validateCreditCard(creditCard);
        CreditCard saved = creditCardRepositoryPort.create(creditCard);

        Log.event(log, "CREDIT_CARD_CREATED", "Cartão de crédito criado. ID: {}", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public CreditCard update(CreditCard creditCard, UUID profileId) {
        validateCreditCard(creditCard);
        CreditCard updated = creditCardRepositoryPort.update(creditCard);

        Log.event(log, "CREDIT_CARD_UPDATED", "Cartão de crédito atualizado. ID: {}", updated.getId());

        return updated;
    }

    @Override
    public CreditCard findByIdAndProfileId(UUID id, UUID profileId) {
        return creditCardRepositoryPort.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado."));
    }

    @Override
    public List<CreditCard> findAllByProfileId(UUID profileId) {
        return creditCardRepositoryPort.findAllByProfileId(profileId);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID profileId) {
        if (!creditCardRepositoryPort.existsByIdAndProfileId(id, profileId)) {
            throw new ResourceNotFoundException("Cartão de crédito não encontrado.");
        }
        creditCardRepositoryPort.delete(id);

        Log.event(log, "CREDIT_CARD_DELETED", "Cartão de crédito excluído. ID: {}", id);
    }

    private void validateCreditCard(CreditCard creditCard) {
        if (creditCard.getInstitution() != null && creditCard.getInstitution().getId() != null) {
            if (!institutionRepositoryPort.existsById(creditCard.getInstitution().getId())) {
                log.warn("Validação falhou: Instituição financeira inexistente.");
                throw new ResourceNotFoundException("Instituição financeira não encontrada.");
            }
        }

        if (creditCardRepositoryPort.isNameTaken(creditCard.getName(), creditCard.getProfileId(), creditCard.getId())) {
            log.warn("Validação falhou: Nome de cartão duplicado '{}'", creditCard.getName());
            throw new ResourceAlreadyExistsException("Um cartão de crédito com este nome já existe para este perfil.");
        }

        if (creditCard.getCreditLimit() != null && creditCard.getCreditLimit().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new DomainException("O limite de crédito não pode ser negativo.");
        }

        if (creditCard.getClosingDay() != null && creditCard.getDueDay() != null && creditCard.getClosingDay().equals(creditCard.getDueDay())) {
            throw new DomainException("O dia de fechamento e o dia de vencimento não podem ser iguais.");
        }
    }
}
