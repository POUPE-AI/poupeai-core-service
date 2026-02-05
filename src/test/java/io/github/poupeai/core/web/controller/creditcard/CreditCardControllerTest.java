package io.github.poupeai.core.web.controller.creditcard;

import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.port.business.CreditCardServicePort;
import io.github.poupeai.core.web.dto.creditcard.CreditCardRequest;
import io.github.poupeai.core.web.dto.creditcard.CreditCardResponse;
import io.github.poupeai.core.web.dto.creditcard.CreditCardUpdateRequest;
import io.github.poupeai.core.web.mapper.creditcard.CreditCardControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardControllerTest {

    @Mock
    private CreditCardServicePort creditCardServicePort;

    @Mock
    private CreditCardControllerMapper creditCardMapper;

    @InjectMocks
    private CreditCardController creditCardController;

    @Test
    @DisplayName("Should get all credit cards for user")
    void shouldGetCreditCardsSuccessfully() {
        String userId = UUID.randomUUID().toString();
        List<CreditCard> creditCards = List.of(new CreditCard());
        List<CreditCardResponse> responses = List.of(new CreditCardResponse(null, null, null, null, null, null, null, null, null));

        when(creditCardServicePort.findAllByProfileId(UUID.fromString(userId))).thenReturn(creditCards);
        when(creditCardMapper.toResponseList(creditCards)).thenReturn(responses);

        ResponseEntity<List<CreditCardResponse>> result = creditCardController.list(userId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(responses, result.getBody());
    }

    @Test
    @DisplayName("Should get credit card by id successfully")
    void shouldGetCreditCardByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        CreditCard card = CreditCard.builder().id(cardId).profileId(userId).build();
        CreditCardResponse response = new CreditCardResponse(cardId, null, null, null, null, null, null, null, null);

        when(creditCardServicePort.findByIdAndProfileId(cardId, userId)).thenReturn(card);
        when(creditCardMapper.toResponse(card)).thenReturn(response);

        ResponseEntity<CreditCardResponse> result = creditCardController.getById(userId.toString(), cardId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @DisplayName("Should create credit card successfully")
    void shouldCreateCreditCardSuccessfully() {
        String userId = UUID.randomUUID().toString();
        CreditCardRequest request = new CreditCardRequest();
        CreditCard card = new CreditCard();
        CreditCard savedCard = new CreditCard();
        CreditCardResponse response = new CreditCardResponse(null, null, null, null, null, null, null, null, null);

        when(creditCardMapper.toDomain(eq(request), any(UUID.class))).thenReturn(card);
        when(creditCardServicePort.create(card)).thenReturn(savedCard);
        when(creditCardMapper.toResponse(savedCard)).thenReturn(response);

        ResponseEntity<CreditCardResponse> result = creditCardController.create(userId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(creditCardServicePort).create(card);
    }

    @Test
    @DisplayName("Should update credit card successfully")
    void shouldUpdateCreditCardSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        CreditCardUpdateRequest request = new CreditCardUpdateRequest();
        CreditCard card = CreditCard.builder().id(cardId).profileId(userId).build();
        CreditCardResponse response = new CreditCardResponse(cardId, null, null, null, null, null, null, null, null);

        when(creditCardServicePort.findByIdAndProfileId(cardId, userId)).thenReturn(card);
        doNothing().when(creditCardMapper).updateDomainFromDto(eq(request), eq(card));
        when(creditCardServicePort.update(card, userId)).thenReturn(card);
        when(creditCardMapper.toResponse(card)).thenReturn(response);

        ResponseEntity<CreditCardResponse> result = creditCardController.update(userId.toString(), cardId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(creditCardServicePort).update(card, userId);
    }

    @Test
    @DisplayName("Should delete credit card successfully")
    void shouldDeleteCreditCardSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        ResponseEntity<Void> result = creditCardController.delete(userId.toString(), cardId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(creditCardServicePort).delete(cardId, userId);
    }
}
