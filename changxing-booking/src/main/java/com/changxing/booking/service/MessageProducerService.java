package com.changxing.booking.service;

import com.changxing.booking.config.RabbitMQConfig;
import com.changxing.booking.dto.BookingCreateMessage;
import com.changxing.booking.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送下单异步落库消息
     * 场景：下单接口只做校验 + Redis 预占，把真实落库交给消费者削峰
     */
    public void sendBookingCreateMessage(BookingCreateMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_CREATE_KEY,
                    message);
            log.info("下单创建消息发送成功: orderNo={}", message.getOrderNo());
        } catch (Exception e) {
            log.error("下单创建消息发送失败: orderNo={}", message.getOrderNo(), e);
        }
    }

    /**
     * 发送订单状态变更消息
     * 场景：订单创建、确认、取消等状态变更时通知车辆服务
     */
    public void sendBookingStatusMessage(Long bookingId, String orderNo, Long userId, 
                                          Long carId, Long trimId, String status, 
                                          String carName, String pickupProvince) {
        try {
            MessageDTO message = MessageDTO.builder()
                    .bookingId(bookingId)
                    .orderNo(orderNo)
                    .userId(userId)
                    .carId(carId)
                    .trimId(trimId)
                    .status(status)
                    .carName(carName)
                    .pickupProvince(pickupProvince)
                    .timestamp(LocalDateTime.now())
                    .message("订单状态变更: " + status)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_STATUS_KEY,
                    message
            );
            log.info("订单状态消息发送成功: bookingId={}, status={}", bookingId, status);
        } catch (Exception e) {
            log.error("订单状态消息发送失败: bookingId={}", bookingId, e);
        }
    }

    /**
     * 发送订单完成消息
     * 场景：订单完成时通知用户服务更新积分，通知评价服务
     */
    public void sendBookingCompletedMessage(Long bookingId, String orderNo, Long userId, 
                                             Long carId, Long trimId, Integer totalDays,
                                             String pickupProvince, String returnProvince) {
        try {
            MessageDTO message = MessageDTO.builder()
                    .bookingId(bookingId)
                    .orderNo(orderNo)
                    .userId(userId)
                    .carId(carId)
                    .trimId(trimId)
                    .totalDays(totalDays)
                    .pickupProvince(pickupProvince)
                    .returnProvince(returnProvince)
                    .timestamp(LocalDateTime.now())
                    .message("订单已完成")
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BOOKING_EXCHANGE,
                    RabbitMQConfig.BOOKING_COMPLETED_KEY,
                    message
            );
            log.info("订单完成消息发送成功: bookingId={}", bookingId);
        } catch (Exception e) {
            log.error("订单完成消息发送失败: bookingId={}", bookingId, e);
        }
    }
}
