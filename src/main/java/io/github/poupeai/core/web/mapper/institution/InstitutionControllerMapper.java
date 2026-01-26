package io.github.poupeai.core.web.mapper.institution;

import io.github.poupeai.core.domain.model.Institution;
import io.github.poupeai.core.web.dto.institution.InstitutionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstitutionControllerMapper {
    InstitutionResponse toResponse(Institution domain);

    List<InstitutionResponse> toResponseList(List<Institution> institutions);
}
