package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "creditCardId", source = "creditCard.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "invoiceId", source = "invoice.id")
    Transaction toDomain(TransactionEntity entity);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "creditCard", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    TransactionEntity toEntity(Transaction domain);

    List<Transaction> toDomainList(List<TransactionEntity> entities);
    
    List<TransactionEntity> toEntityList(List<Transaction> domains);
}
