package io.github.poupeai.core.web.mapper.transaction;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.web.dto.category.CategorySummary;
import io.github.poupeai.core.web.dto.transaction.CreateTransactionRequest;
import io.github.poupeai.core.web.dto.transaction.InternalCreateTransactionRequest;
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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", source = "date")
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "originalStatementDescription", source = "description")
    @Mapping(target = "creditCardId", ignore = true)
    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "isInstallment", constant = "false")
    @Mapping(target = "installmentNumber", ignore = true)
    @Mapping(target = "totalInstallments", ignore = true)
    @Mapping(target = "purchaseGroupUuid", ignore = true)
    @Mapping(target = "attachmentKey", ignore = true)
    @Mapping(target = "attachmentUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toDomain(InternalCreateTransactionRequest request);

    @Mapping(target = "category", source = "category")
    TransactionResponse toResponse(Transaction domain);

    List<TransactionResponse> toResponseList(List<Transaction> domains);

    List<Transaction> toDomainListFromInternal(List<InternalCreateTransactionRequest> requests);

    CategorySummary mapCategory(Category category);
}
