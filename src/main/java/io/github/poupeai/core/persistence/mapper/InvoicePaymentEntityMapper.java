package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.InvoicePayment;
import io.github.poupeai.core.persistence.entity.InvoicePaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoicePaymentEntityMapper {
    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "paymentTransactionId", source = "paymentTransaction.id")
    InvoicePayment toDomain(InvoicePaymentEntity entity);

    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "paymentTransaction", ignore = true)
    InvoicePaymentEntity toEntity(InvoicePayment domain);

    List<InvoicePayment> toDomainList(List<InvoicePaymentEntity> entities);
}
