package io.github.poupeai.core.web.mapper.invoicepayment;

import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.web.dto.invoicepayment.InvoicePaymentResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoicePaymentControllerMapper {
    InvoicePaymentResponse toResponse(InvoicePayment domain);
    List<InvoicePaymentResponse> toResponseList(List<InvoicePayment> domains);
}
