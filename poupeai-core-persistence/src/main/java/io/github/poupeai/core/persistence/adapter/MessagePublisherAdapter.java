package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.port.output.MessagePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagePublisherAdapter implements MessagePublisherPort {
    private final RabbitTemplate rabbitTemplate;

    @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Override
    public void publish(Object message, String routingKey) {
        try {
            log.info("Publicando mensagem para o RabbitMQ com exchange: {} e routingKey: {}", exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
                String correlationId = org.slf4j.MDC.get("traceId");
                if (correlationId == null) {
                    correlationId = java.util.UUID.randomUUID().toString();
                }
                m.getMessageProperties().setCorrelationId(correlationId);
                return m;
            });

        } catch (Exception e) {
            log.error("Erro ao publicar mensagem para o RabbitMQ", e);
            throw new RuntimeException("Falha ao publicar mensagem", e);
        }
    }
}
