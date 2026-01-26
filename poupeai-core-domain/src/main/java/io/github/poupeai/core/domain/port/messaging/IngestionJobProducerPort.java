package io.github.poupeai.core.domain.port.messaging;

public interface IngestionJobProducerPort {
    void publish(Object message, String routingKey);
}
