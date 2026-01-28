package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.persistence.entity.CreditCardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditCardEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "institution", source = "institution")
    @Mapping(target = "usedCreditLimit", expression = "java(java.math.BigDecimal.ZERO)")
    CreditCard toDomain(CreditCardEntity entity);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "institution", source = "institution")
    CreditCardEntity toEntity(CreditCard domain);

    List<CreditCard> toDomainList(List<CreditCardEntity> entities);
}
