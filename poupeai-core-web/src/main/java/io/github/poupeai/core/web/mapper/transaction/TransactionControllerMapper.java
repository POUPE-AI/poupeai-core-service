package io.github.poupeai.core.web.mapper.transaction;

import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.web.dto.transaction.TransactionRequest;
import io.github.poupeai.core.web.dto.transaction.TransactionResponse;
import io.github.poupeai.core.web.dto.transaction.TransactionUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "installmentNumber", ignore = true)
    @Mapping(target = "purchaseGroupUuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toDomain(TransactionRequest request, UUID profileId);

    TransactionResponse toResponse(Transaction domain);

    List<TransactionResponse> toResponseList(List<Transaction> domains);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "bankAccountId", ignore = true)
    @Mapping(target = "creditCardId", ignore = true)
    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "isInstallment", ignore = true)
    @Mapping(target = "installmentNumber", ignore = true)
    @Mapping(target = "totalInstallments", ignore = true)
    @Mapping(target = "purchaseGroupUuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDomainFromDto(TransactionUpdateRequest request, @MappingTarget Transaction domain);
}
