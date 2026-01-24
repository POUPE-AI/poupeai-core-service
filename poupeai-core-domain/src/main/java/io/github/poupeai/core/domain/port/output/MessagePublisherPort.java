package io.github.poupeai.core.domain.port.output;

public interface MessagePublisherPort {
    void publish(Object message, String routingKey);
}
