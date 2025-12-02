package io.github.poupeai.core.web.controller.profile;

import io.github.poupeai.core.domain.model.Profile;
import io.github.poupeai.core.domain.port.business.GetProfilePort;
import io.github.poupeai.core.web.dto.profile.ProfileResponse;
import io.github.poupeai.core.web.mapper.profile.ProfileControllerMapper;
import io.github.poupeai.core.web.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "Gerenciamento de Perfis de Usuário")
public class ProfileController {
    private final GetProfilePort getProfilePort;
    private final ProfileControllerMapper mapper;

    @GetMapping("/me")
    @Operation(
            summary = "Obter Meu Perfil",
            description = "Retorna os dados do usuário logado com base no Token JWT.",
            security = @SecurityRequirement(name = "bearer-key")
    )
    public ResponseEntity<ProfileResponse> getMyProfile(
            @Parameter(hidden = true) @CurrentUserId String userId
    ) {
        log.info("Buscando perfil para o usuário autenticado: {}", userId);

        UUID uuid = UUID.fromString(userId);

        Profile profile = getProfilePort.execute(uuid);

        return ResponseEntity.ok(mapper.toResponse(profile));
    }
}
