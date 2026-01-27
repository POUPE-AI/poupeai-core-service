package io.github.poupeai.core.persistence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.ingestionjobs.exchange}")
    private String ingestionJobsExchange;

    @Value("${app.rabbitmq.ingestionjobs.queue}")
    private String ingestionJobsQueue;

    @Value("${app.rabbitmq.ingestionjobs.routing-key}")
    private String ingestionJobsRoutingKey;

    @Value("${app.rabbitmq.notifications.exchange}")
    private String notificationsExchange;

    @Value("${app.rabbitmq.notifications.queue}")
    private String notificationsQueue;

    @Value("${app.rabbitmq.notifications.routing-key}")
    private String notificationsRoutingKey;

    @Bean
    public TopicExchange ingestionJobsExchange() {
        return new TopicExchange(ingestionJobsExchange);
    }

    @Bean
    public Queue ingestionJobsQueue() {
        return QueueBuilder.durable(ingestionJobsQueue).build();
    }

    @Bean
    public Binding ingestionJobsBinding() {
        return BindingBuilder
                .bind(ingestionJobsQueue())
                .to(ingestionJobsExchange())
                .with(ingestionJobsRoutingKey);
    }

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(notificationsExchange);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(notificationsQueue).build();
    }

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder
                .bind(notificationsQueue())
                .to(notificationsExchange())
                .with(notificationsRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
