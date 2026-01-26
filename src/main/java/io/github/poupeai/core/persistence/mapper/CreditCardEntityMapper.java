package io.github.poupeai.core.persistence.mapper;

import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.persistence.entity.CreditCardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditCardEntityMapper {
    @Mapping(target = "profileId", source = "profile.userId")
    @Mapping(target = "institutionId", source = "institution.id")
    CreditCard toDomain(CreditCardEntity entity);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "institution", ignore = true)
    CreditCardEntity toEntity(CreditCard domain);

    List<CreditCard> toDomainList(List<CreditCardEntity> entities);
}
