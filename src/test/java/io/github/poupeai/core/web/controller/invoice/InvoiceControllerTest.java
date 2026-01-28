package io.github.poupeai.core.web.controller.invoice;

import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceFilter;
import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.port.business.InvoicePaymentServicePort;
import io.github.poupeai.core.domain.port.business.InvoiceServicePort;
import io.github.poupeai.core.web.dto.common.PageResponse;
import io.github.poupeai.core.web.dto.invoice.InvoiceResponse;
import io.github.poupeai.core.web.dto.invoicepayment.InvoicePaymentRequest;
import io.github.poupeai.core.web.dto.invoicepayment.InvoicePaymentResponse;
import io.github.poupeai.core.web.mapper.invoice.InvoiceControllerMapper;
import io.github.poupeai.core.web.mapper.invoicepayment.InvoicePaymentControllerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock
    private InvoiceServicePort invoiceServicePort;

    @Mock
    private InvoicePaymentServicePort invoicePaymentServicePort;

    @Mock
    private InvoiceControllerMapper invoiceMapper;

    @Mock
    private InvoicePaymentControllerMapper invoicePaymentMapper;

    @InjectMocks
    private InvoiceController invoiceController;

    @Test
    @DisplayName("Should list paginated invoices successfully")
    void shouldListInvoicesSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = createInvoice(invoiceId);

        PageDomain<Invoice> pageDomain = PageDomain.<Invoice>builder()
                .content(List.of(invoice))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        InvoiceResponse response = InvoiceResponse.builder().id(invoiceId).build();
        List<InvoiceResponse> responses = List.of(response);

        when(invoiceServicePort.search(eq(userId), any(InvoiceFilter.class))).thenReturn(pageDomain);
        when(invoiceMapper.toResponseList(anyList())).thenReturn(responses);

        ResponseEntity<PageResponse<InvoiceResponse>> result = invoiceController.list(
                userId.toString(), 0, 10, null, null, null, null, "DESC", "dueDate");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("Should get invoice by id successfully")
    void shouldGetInvoiceByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = createInvoice(invoiceId);
        InvoiceResponse response = InvoiceResponse.builder().id(invoiceId).build();

        when(invoiceServicePort.findById(invoiceId, userId)).thenReturn(invoice);
        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        ResponseEntity<InvoiceResponse> result = invoiceController.getById(userId.toString(), invoiceId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(invoiceId, result.getBody().getId());
    }

    @Test
    @DisplayName("Should delete invoice successfully")
    void shouldDeleteInvoiceSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        ResponseEntity<Void> result = invoiceController.delete(userId.toString(), invoiceId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(invoiceServicePort).deleteInvoice(invoiceId, userId);
    }

    @Test
    @DisplayName("Should register payment successfully")
    void shouldRegisterPaymentSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID bankAccountId = UUID.randomUUID();
        Long paymentId = 1L;
        BigDecimal amount = BigDecimal.valueOf(500);

        InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                .bankAccountId(bankAccountId)
                .amount(amount)
                .build();

        InvoicePayment payment = InvoicePayment.builder()
                .id(paymentId)
                .invoiceId(invoiceId)
                .amount(amount)
                .build();

        InvoicePaymentResponse response = InvoicePaymentResponse.builder()
                .id(paymentId)
                .invoiceId(invoiceId)
                .amount(amount)
                .build();

        when(invoicePaymentServicePort.registerPayment(invoiceId, bankAccountId, amount, userId))
                .thenReturn(payment);
        when(invoicePaymentMapper.toResponse(payment)).thenReturn(response);

        ResponseEntity<InvoicePaymentResponse> result = invoiceController.registerPayment(
                userId.toString(), invoiceId, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(paymentId, result.getBody().getId());
    }

    @Test
    @DisplayName("Should delete payment successfully")
    void shouldDeletePaymentSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        Long paymentId = 1L;

        ResponseEntity<Void> result = invoiceController.deletePayment(userId.toString(), invoiceId, paymentId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(invoicePaymentServicePort).deletePayment(paymentId, userId);
    }

    private Invoice createInvoice(UUID id) {
        return Invoice.builder()
                .id(id)
                .creditCardId(UUID.randomUUID())
                .month(1)
                .year(2026)
                .closingDate(LocalDate.of(2026, 1, 10))
                .dueDate(LocalDate.of(2026, 2, 10))
                .totalAmount(BigDecimal.valueOf(1000))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .build();
    }
}