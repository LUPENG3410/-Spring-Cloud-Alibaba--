package com.changxing.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    
    // 队列名称
    public static final String BOOKING_STATUS_QUEUE = "booking.status.queue";
    public static final String BOOKING_COMPLETED_QUEUE = "booking.completed.queue";
    
    // 路由键
    public static final String BOOKING_STATUS_KEY = "booking.status";
    public static final String BOOKING_COMPLETED_KEY = "booking.completed";

    // 下单异步落库队列（削峰）
    public static final String BOOKING_CREATE_QUEUE = "booking.create.queue";
    public static final String BOOKING_CREATE_KEY = "booking.create";
    public static final String BOOKING_CREATE_DLX = "booking.create.dlx";
    public static final String BOOKING_CREATE_DLQ = "booking.create.dlq";
    public static final String BOOKING_CREATE_DLQ_KEY = "booking.create.dlq";

    // 声明交换机
    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(BOOKING_EXCHANGE, true, false);
    }

    // 声明队列
    @Bean
    public Queue bookingStatusQueue() {
        return QueueBuilder.durable(BOOKING_STATUS_QUEUE).build();
    }

    @Bean
    public Queue bookingCompletedQueue() {
        return QueueBuilder.durable(BOOKING_COMPLETED_QUEUE).build();
    }

    // 绑定队列到交换机
    @Bean
    public Binding bookingStatusBinding(Queue bookingStatusQueue, DirectExchange bookingExchange) {
        return BindingBuilder.bind(bookingStatusQueue).to(bookingExchange).with(BOOKING_STATUS_KEY);
    }

    @Bean
    public Binding bookingCompletedBinding(Queue bookingCompletedQueue, DirectExchange bookingExchange) {
        return BindingBuilder.bind(bookingCompletedQueue).to(bookingExchange).with(BOOKING_COMPLETED_KEY);
    }

    @Bean
    public Queue bookingCreateQueue() {
        return QueueBuilder.durable(BOOKING_CREATE_QUEUE)
                .deadLetterExchange(BOOKING_CREATE_DLX)
                .deadLetterRoutingKey(BOOKING_CREATE_DLQ_KEY)
                .build();
    }

    @Bean
    public DirectExchange bookingCreateDlx() {
        return new DirectExchange(BOOKING_CREATE_DLX, true, false);
    }

    @Bean
    public Queue bookingCreateDlq() {
        return QueueBuilder.durable(BOOKING_CREATE_DLQ).build();
    }

    @Bean
    public Binding bookingCreateBinding(Queue bookingCreateQueue, DirectExchange bookingExchange) {
        return BindingBuilder.bind(bookingCreateQueue).to(bookingExchange).with(BOOKING_CREATE_KEY);
    }

    @Bean
    public Binding bookingCreateDlqBinding(Queue bookingCreateDlq, DirectExchange bookingCreateDlx) {
        return BindingBuilder.bind(bookingCreateDlq).to(bookingCreateDlx).with(BOOKING_CREATE_DLQ_KEY);
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
