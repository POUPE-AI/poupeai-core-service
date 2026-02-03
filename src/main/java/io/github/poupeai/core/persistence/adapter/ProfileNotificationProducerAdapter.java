package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.port.messaging.ProfileNotificationProducerPort;
import io.github.poupeai.core.persistence.messaging.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileNotificationProducerAdapter implements ProfileNotificationProducerPort {
    private final RabbitMQPublisher rabbitMQPublisher;

    @Value("${app.rabbitmq.notifications.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notifications.routing-key}")
    private String routingKey;

    @Override
    public void publishDeletionScheduled(PoupeAiEvent<?> event) {
        log.info("Publicando evento PROFILE_DELETION_SCHEDULED para usuário: {}",
                event.getRecipient() != null ? event.getRecipient().getUserId() : "N/A");
        rabbitMQPublisher.publish(exchange, routingKey, event);
    }
}
