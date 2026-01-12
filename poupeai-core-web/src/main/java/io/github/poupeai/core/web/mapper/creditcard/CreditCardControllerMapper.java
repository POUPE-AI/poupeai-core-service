package io.github.poupeai.core.web.mapper.creditcard;

import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.web.dto.creditcard.CreditCardRequest;
import io.github.poupeai.core.web.dto.creditcard.CreditCardResponse;
import io.github.poupeai.core.web.dto.creditcard.CreditCardUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CreditCardControllerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CreditCard toDomain(CreditCardRequest request, UUID profileId);

    CreditCardResponse toResponse(CreditCard domain);

    List<CreditCardResponse> toResponseList(List<CreditCard> domains);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateDomainFromDto(CreditCardUpdateRequest request, @MappingTarget CreditCard domain);
}
