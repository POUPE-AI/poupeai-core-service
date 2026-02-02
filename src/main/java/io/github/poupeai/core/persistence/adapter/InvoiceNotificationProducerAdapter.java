package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.event.PoupeAiEvent;
import io.github.poupeai.core.domain.port.messaging.InvoiceNotificationProducerPort;
import io.github.poupeai.core.persistence.messaging.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceNotificationProducerAdapter implements InvoiceNotificationProducerPort {
    private final RabbitMQPublisher rabbitMQPublisher;

    @Value("${app.rabbitmq.notifications.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notifications.routing-key}")
    private String routingKey;

    @Override
    public void publishDueSoon(PoupeAiEvent<?> event) {
        rabbitMQPublisher.publish(exchange, routingKey, event);
    }

    @Override
    public void publishOverdue(PoupeAiEvent<?> event) {
        rabbitMQPublisher.publish(exchange, routingKey, event);
    }
}
