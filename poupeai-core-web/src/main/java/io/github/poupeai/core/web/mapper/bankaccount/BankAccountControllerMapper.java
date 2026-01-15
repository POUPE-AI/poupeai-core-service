package io.github.poupeai.core.web.mapper.bankaccount;

import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountRequest;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountResponse;
import io.github.poupeai.core.web.dto.bankaccount.BankAccountUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BankAccountControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BankAccount toDomain(BankAccountRequest request, UUID profileId);

    BankAccountResponse toResponse(BankAccount domain);

    List<BankAccountResponse> toResponseList(List<BankAccount> domains);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "initialBalance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDomainFromDto(BankAccountUpdateRequest request, @MappingTarget BankAccount domain);
}
