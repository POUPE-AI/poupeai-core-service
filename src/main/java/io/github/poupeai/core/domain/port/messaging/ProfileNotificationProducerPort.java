package io.github.poupeai.core.domain.port.messaging;

import io.github.poupeai.core.domain.event.PoupeAiEvent;

public interface ProfileNotificationProducerPort {
    void publishDeletionScheduled(PoupeAiEvent<?> event);
}
