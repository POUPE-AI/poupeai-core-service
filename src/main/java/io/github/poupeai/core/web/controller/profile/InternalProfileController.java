package io.github.poupeai.core.web.controller.profile;

import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.CreateOrUpdateProfilePort;
import io.github.poupeai.core.web.dto.profile.ProfileRequest;
import io.github.poupeai.core.web.dto.profile.ProfileResponse;
import io.github.poupeai.core.web.mapper.profile.ProfileControllerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/profiles")
@RequiredArgsConstructor
@Tag(name = "Internal Profiles", description = "Endpoints internos para gestão de perfis (System-to-System)")
public class InternalProfileController {
    private final CreateOrUpdateProfilePort createOrUpdateProfilePort;
    private final ProfileControllerMapper mapper;

    @PostMapping
    @Operation(summary = "Criar ou Atualizar Perfil (Sync)", description = "Recebe dados do Keycloak para sincronização. Requer API Key Interna.")
    public ResponseEntity<ProfileResponse> createOrUpdate(@RequestBody @Valid ProfileRequest request) {
        Profile domain = mapper.toDomain(request);
        Profile saved = createOrUpdateProfilePort.execute(domain);

        return ResponseEntity.ok(mapper.toResponse(saved));
    }
}
