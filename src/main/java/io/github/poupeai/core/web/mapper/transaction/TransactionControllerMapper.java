package io.github.poupeai.core.web.mapper.transaction;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.web.dto.category.CategorySummary;
import io.github.poupeai.core.web.dto.transaction.CreateTransactionRequest;
import io.github.poupeai.core.web.dto.transaction.TransactionResponse;
import io.github.poupeai.core.web.dto.transaction.UpdateTransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionControllerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "category.id", source = "request.categoryId")
    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "installmentNumber", ignore = true)
    @Mapping(target = "purchaseGroupUuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toDomain(CreateTransactionRequest request, UUID profileId);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "bankAccountId", ignore = true)
    @Mapping(target = "creditCardId", ignore = true)
    @Mapping(target = "category.id", source = "request.categoryId")
    @Mapping(target = "isInstallment", ignore = true)
    Transaction toDomain(UpdateTransactionRequest request, UUID id);

    @Mapping(target = "category", source = "category")
    TransactionResponse toResponse(Transaction domain);

    List<TransactionResponse> toResponseList(List<Transaction> domains);

    CategorySummary mapCategory(Category category);
}
