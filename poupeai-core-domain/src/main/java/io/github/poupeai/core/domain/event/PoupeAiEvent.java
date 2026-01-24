package io.github.poupeai.core.domain.event;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PoupeAiEvent<T> {
    private UUID messageId;
    private OffsetDateTime timestamp;
    private String triggerType;
    private String eventType;
    private Recipient recipient;
    private T payload;

    @Data
    @Builder
    public static class Recipient {
        private String userId;
        private String email;
        private String name;
    }
}
