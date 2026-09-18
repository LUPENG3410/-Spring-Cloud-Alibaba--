package com.changxing.booking.consumer;

import com.changxing.booking.config.RabbitMQConfig;
import com.changxing.booking.dto.BookingCreateMessage;
import com.changxing.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCreateConsumer {

    private final BookingService bookingService;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_CREATE_QUEUE)
    public void handleCreateMessage(BookingCreateMessage message) {
        try {
            log.info("收到下单创建消息: orderNo={}", message.getOrderNo());
            bookingService.processBookingCreate(message.getRequest(), message.getUserId(), message.getOrderNo());
        } catch (Exception e) {
            log.error("处理下单创建消息失败: orderNo={}", message.getOrderNo(), e);
            // 拒绝且不重投，交给死信队列
            throw new AmqpRejectAndDontRequeueException("下单创建处理失败: " + message.getOrderNo(), e);
        }
    }
}