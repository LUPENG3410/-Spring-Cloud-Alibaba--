package com.changxing.car.consumer;

import com.changxing.car.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMessageConsumer {

    private final CarService carService;

    /**
     * 监听订单状态变更消息
     * 场景：订单创建、确认、取消等状态变更时更新车辆状态
     */
    @RabbitListener(queues = "booking.status.queue")
    public void handleBookingStatusMessage(Map<String, Object> message) {
        try {
            log.info("收到订单状态消息: {}", message);
            
            Long carId = Long.valueOf(message.get("carId").toString());
            Long trimId = message.get("trimId") != null ? Long.valueOf(message.get("trimId").toString()) : carId;
            String status = message.get("status").toString();
            String pickupProvince = (String) message.get("pickupProvince");
            
            // 根据订单状态更新车辆状态
            String carStatus = mapBookingStatusToCarStatus(status);
            if (carStatus != null) {
                carService.updateCarStatus(trimId, carStatus);
                log.info("车辆状态已更新: trimId={}, status={}", trimId, carStatus);
            }
            
            // 如果是待确认状态，更新车辆当前位置
            if ("pending".equals(status) && pickupProvince != null) {
                carService.updateCarProvince(trimId, null, pickupProvince);
                log.info("车辆当前位置已更新: trimId={}, province={}", trimId, pickupProvince);
            }
            
        } catch (Exception e) {
            log.error("处理订单状态消息失败: {}", message, e);
        }
    }

    /**
     * 将订单状态映射为车辆状态
     */
    private String mapBookingStatusToCarStatus(String bookingStatus) {
        switch (bookingStatus) {
            case "pending":
            case "confirmed":
                return "reserved";  // 已预约
            case "active":
                return "rented";    // 已租出
            case "completed":
            case "cancelled":
                return "available"; // 可用
            default:
                return null;
        }
    }
}
