package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.persistence.entity.BankAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "institution", source = "institution")
    BankAccount toDomain(BankAccountEntity entity);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "institution", ignore = true)
    BankAccountEntity toEntity(BankAccount domain);

    List<BankAccount> toDomainList(List<BankAccountEntity> entities);
}
