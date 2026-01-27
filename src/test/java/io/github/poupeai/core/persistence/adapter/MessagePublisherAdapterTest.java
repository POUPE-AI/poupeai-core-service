package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.persistence.messaging.RabbitMQPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQPublisher publisher;

    @Test
    @DisplayName("Should publish message with correlation id from MDC")
    void shouldPublishWithCorrelationIdFromMdc() {
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        MDC.put("traceId", "trace-123");
        try {
            publisher.publish("test-exchange", "rk", "payload");
        } finally {
        }

        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("rk"), eq("payload"), postProcessorCaptor.capture());

        MessagePostProcessor mpp = postProcessorCaptor.getValue();
        MessageProperties props = new MessageProperties();
        Message message = new Message(new byte[0], props);

        try {
            Message processed = mpp.postProcessMessage(message);

            assertNotNull(processed.getMessageProperties().getCorrelationId());
            assertEquals("trace-123", processed.getMessageProperties().getCorrelationId());
        } finally {
            MDC.remove("traceId");
        }
    }

    @Test
    @DisplayName("Should publish message with generated correlation id when MDC is empty")
    void shouldPublishWithGeneratedCorrelationIdWhenMdcEmpty() {
        MDC.remove("traceId");
        publisher.publish("test-exchange", "rk", "payload");

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("rk"), eq("payload"), postProcessorCaptor.capture());

        MessagePostProcessor mpp = postProcessorCaptor.getValue();
        MessageProperties props = new MessageProperties();
        Message message = new Message(new byte[0], props);

        Message processed = mpp.postProcessMessage(message);

        assertNotNull(processed.getMessageProperties().getCorrelationId());
        assertFalse(processed.getMessageProperties().getCorrelationId().isBlank());
    }

    @Test
    @DisplayName("Should throw RuntimeException when publishing fails")
    void shouldThrowRuntimeExceptionWhenPublishingFails() {
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq("test-exchange"), eq("rk"), eq("payload"), any(MessagePostProcessor.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> publisher.publish("test-exchange", "rk", "payload"));
        assertEquals("Falha ao publicar mensagem", ex.getMessage());
    }
}

@ExtendWith(MockitoExtension.class)
class IngestionJobProducerAdapterTest {

    @Mock
    private RabbitMQPublisher rabbitMQPublisher;

    @InjectMocks
    private IngestionJobProducerAdapter adapter;

    @Test
    @DisplayName("Should publish message delegating to RabbitMQPublisher")
    void shouldPublishDelegatingToPublisher() {
        ReflectionTestUtils.setField(adapter, "exchange", "test-exchange");

        adapter.publish("payload", "rk");

        verify(rabbitMQPublisher).publish("test-exchange", "rk", "payload");
    }

    @Test
    @DisplayName("Should propagate exception when publisher fails")
    void shouldPropagateExceptionWhenPublisherFails() {
        ReflectionTestUtils.setField(adapter, "exchange", "test-exchange");

        doThrow(new RuntimeException("Falha ao publicar mensagem"))
                .when(rabbitMQPublisher)
                .publish("test-exchange", "rk", "payload");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adapter.publish("payload", "rk"));
        assertEquals("Falha ao publicar mensagem", ex.getMessage());
    }
}
