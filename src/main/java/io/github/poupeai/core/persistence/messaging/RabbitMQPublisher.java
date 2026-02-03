package io.github.poupeai.core.persistence.messaging;

import io.github.poupeai.core.audit.Log;
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
            rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
                String correlationId = MDC.get("trace.correlation_id");
                if (correlationId == null) {
                    correlationId = UUID.randomUUID().toString();
                }
                m.getMessageProperties().setCorrelationId(correlationId);
                return m;
            });
        } catch (Exception e) {
            Log.error(log, "RABBITMQ_PUBLISH_FAIL", "Erro ao publicar mensagem no RabbitMQ", e);
            throw new RuntimeException("Falha ao publicar mensagem", e);
        }
    }
}
