package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.persistence.entity.InvoiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceEntityMapper {
    @Mapping(target = "creditCardId", source = "creditCard.id")
    Invoice toDomain(InvoiceEntity entity);

    @Mapping(target = "creditCard", ignore = true)
    InvoiceEntity toEntity(Invoice domain);

    List<Invoice> toDomainList(List<InvoiceEntity> entities);
}
