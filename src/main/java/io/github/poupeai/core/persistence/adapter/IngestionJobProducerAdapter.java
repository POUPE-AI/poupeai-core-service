package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.port.messaging.IngestionJobProducerPort;
import io.github.poupeai.core.persistence.messaging.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionJobProducerAdapter implements IngestionJobProducerPort {
    private final RabbitMQPublisher rabbitMQPublisher;

    @Value("${app.rabbitmq.ingestionjobs.exchange}")
    private String exchange;

    @Override
    public void publish(Object message, String routingKey) {
        rabbitMQPublisher.publish(exchange, routingKey, message);
    }
}
