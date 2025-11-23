package io.github.poupeai.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isDeactivated;
    private OffsetDateTime deactivationScheduledAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
