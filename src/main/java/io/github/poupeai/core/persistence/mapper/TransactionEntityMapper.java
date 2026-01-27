package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionEntityMapper {

    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "creditCardId", source = "creditCard.id")
    @Mapping(target = "invoiceId", source = "invoice.id")
    Transaction toDomain(TransactionEntity entity);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "creditCard", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    TransactionEntity toEntity(Transaction domain);

    List<Transaction> toDomainList(List<TransactionEntity> entities);
}
