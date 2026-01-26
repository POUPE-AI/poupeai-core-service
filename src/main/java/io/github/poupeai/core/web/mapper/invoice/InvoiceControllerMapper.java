package io.github.poupeai.core.web.mapper.invoice;

import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.web.dto.invoice.InvoiceResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceControllerMapper {
    InvoiceResponse toResponse(Invoice domain);

    List<InvoiceResponse> toResponseList(List<Invoice> domains);
}
