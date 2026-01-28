package io.github.poupeai.core.web.controller.creditcard;

import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.port.business.CreditCardServicePort;
import io.github.poupeai.core.web.dto.creditcard.CreditCardRequest;
import io.github.poupeai.core.web.dto.creditcard.CreditCardResponse;
import io.github.poupeai.core.web.dto.creditcard.CreditCardUpdateRequest;
import io.github.poupeai.core.web.mapper.creditcard.CreditCardControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
@Tag(name = "Cartões de Crédito", description = "Gerenciamento de Cartões de Crédito")
public class CreditCardController {
    private final CreditCardServicePort creditCardServicePort;
    private final CreditCardControllerMapper creditCardMapper;

    @GetMapping
    @Operation(
        summary = "Obter meus cartões de crédito",
        description = "Retorna todos os cartões de crédito do usuário",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<List<CreditCardResponse>> list(
        @Parameter(hidden = true) @CurrentUserId String userId) {

        List<CreditCard> creditCards = creditCardServicePort.findAllByProfileId(UUID.fromString(userId));
        return ResponseEntity.ok(creditCardMapper.toResponseList(creditCards));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obter cartão de crédito por ID",
        description = "Retorna detalhes de um cartão de crédito específico",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CreditCardResponse> getById(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        CreditCard creditCard = creditCardServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        return ResponseEntity.ok(creditCardMapper.toResponse(creditCard));
    }

    @PostMapping
    @Operation(
        summary = "Criar cartão de crédito",
        description = "Cria um novo cartão de crédito",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CreditCardResponse> create(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @RequestBody @Valid CreditCardRequest request) {

        CreditCard creditCard = creditCardMapper.toDomain(request, UUID.fromString(userId));
        CreditCard savedCreditCard = creditCardServicePort.create(creditCard);

        return ResponseEntity.ok(creditCardMapper.toResponse(savedCreditCard));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Atualizar cartão de crédito",
        description = "Atualiza os dados de um cartão de crédito existente",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<CreditCardResponse> update(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id, @RequestBody @Valid CreditCardUpdateRequest request) {

        CreditCard creditCard = creditCardServicePort.findByIdAndProfileId(id, UUID.fromString(userId));
        creditCardMapper.updateDomainFromDto(request, creditCard);
        CreditCard updatedCreditCard = creditCardServicePort.update(creditCard, UUID.fromString(userId));

        return ResponseEntity.ok(creditCardMapper.toResponse(updatedCreditCard));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar cartão de crédito",
        description = "Deleta um cartão de crédito específico",
        security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<Void> delete(
        @Parameter(hidden = true) @CurrentUserId String userId,
        @PathVariable UUID id) {

        creditCardServicePort.delete(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
