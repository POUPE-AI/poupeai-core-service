package io.github.poupeai.core.persistence.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.exchange}")
    private String exchange;

    @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.queue}")
    private String queue;

    @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public org.springframework.amqp.core.TopicExchange exchange() {
        return new org.springframework.amqp.core.TopicExchange(exchange);
    }

    @Bean
    public org.springframework.amqp.core.Queue queue() {
        return org.springframework.amqp.core.QueueBuilder.durable(queue).build();
    }

    @Bean
    public org.springframework.amqp.core.Binding binding() {
        return org.springframework.amqp.core.BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
