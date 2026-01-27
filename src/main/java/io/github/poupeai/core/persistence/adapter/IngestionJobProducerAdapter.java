package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.port.messaging.IngestionJobProducerPort;
import io.github.poupeai.core.persistence.messaging.RabbitMQPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngestionJobProducerAdapter implements IngestionJobProducerPort {
    private final RabbitMQPublisher rabbitMQPublisher;

    @Value("${app.rabbitmq.ingestionjobs.exchange}")
    private String exchange;

    @Override
    public void publish(Object message, String routingKey) {
        log.info("Publicando evento de ingestion job com routingKey: {}", routingKey);
        rabbitMQPublisher.publish(exchange, routingKey, message);
    }
}
