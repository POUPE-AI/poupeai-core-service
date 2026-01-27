package io.github.poupeai.core.domain.port.messaging;

import io.github.poupeai.core.domain.event.PoupeAiEvent;

public interface InvoiceNotificationProducerPort {
    void publishDueSoon(PoupeAiEvent<?> event);
    void publishOverdue(PoupeAiEvent<?> event);
}
