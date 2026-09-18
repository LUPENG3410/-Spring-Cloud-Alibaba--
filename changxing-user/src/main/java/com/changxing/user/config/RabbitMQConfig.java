package com.changxing.user.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机名称（与订单服务一致）
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    
    // 队列名称
    public static final String BOOKING_COMPLETED_QUEUE = "booking.completed.queue";
    
    // 路由键
    public static final String BOOKING_COMPLETED_KEY = "booking.completed";

    // 声明交换机
    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(BOOKING_EXCHANGE, true, false);
    }

    // 声明队列
    @Bean
    public Queue bookingCompletedQueue() {
        return QueueBuilder.durable(BOOKING_COMPLETED_QUEUE).build();
    }

    // 绑定队列到交换机
    @Bean
    public Binding bookingCompletedBinding(Queue bookingCompletedQueue, DirectExchange bookingExchange) {
        return BindingBuilder.bind(bookingCompletedQueue).to(bookingExchange).with(BOOKING_COMPLETED_KEY);
    }

    // 使用JSON序列化
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
