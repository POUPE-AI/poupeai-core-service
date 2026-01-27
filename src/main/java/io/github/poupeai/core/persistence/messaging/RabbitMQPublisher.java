package io.github.poupeai.core.persistence.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publish(String exchange, String routingKey, Object message) {
        try {
            log.info("Publicando mensagem para o RabbitMQ - exchange: {}, routingKey: {}", exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
                String correlationId = MDC.get("traceId");
                if (correlationId == null) {
                    correlationId = UUID.randomUUID().toString();
                }
                m.getMessageProperties().setCorrelationId(correlationId);
                return m;
            });
            log.debug("Mensagem publicada com sucesso");
        } catch (Exception e) {
            log.error("Erro ao publicar mensagem para o RabbitMQ - exchange: {}, routingKey: {}", exchange, routingKey, e);
            throw new RuntimeException("Falha ao publicar mensagem", e);
        }
    }
}
