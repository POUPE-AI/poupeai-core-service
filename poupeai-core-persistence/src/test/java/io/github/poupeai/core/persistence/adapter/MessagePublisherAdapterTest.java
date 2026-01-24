package io.github.poupeai.core.persistence.adapter;

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
class MessagePublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MessagePublisherAdapter adapter;

    @Test
    @DisplayName("Should publish message with correlation id from MDC")
    void shouldPublishWithCorrelationIdFromMdc() {
        ReflectionTestUtils.setField(adapter, "exchange", "test-exchange");

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        MDC.put("traceId", "trace-123");
        try {
            adapter.publish("payload", "rk");
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
        ReflectionTestUtils.setField(adapter, "exchange", "test-exchange");

        MDC.remove("traceId");
        adapter.publish("payload", "rk");

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
        ReflectionTestUtils.setField(adapter, "exchange", "test-exchange");

        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq("test-exchange"), eq("rk"), eq("payload"), any(MessagePostProcessor.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adapter.publish("payload", "rk"));
        assertEquals("Falha ao publicar mensagem", ex.getMessage());
    }
}
