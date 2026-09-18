package com.changxing.user.consumer;

import com.changxing.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMessageConsumer {

    private final UserService userService;

    /**
     * 监听订单完成消息
     * 场景：订单完成时更新用户积分和会员等级
     */
    @RabbitListener(queues = "booking.completed.queue")
    public void handleBookingCompletedMessage(Map<String, Object> message) {
        try {
            log.info("收到订单完成消息: {}", message);
            
            Long userId = Long.valueOf(message.get("userId").toString());
            Integer totalDays = message.get("totalDays") != null ? 
                    Integer.valueOf(message.get("totalDays").toString()) : 0;
            String returnProvince = (String) message.get("returnProvince");
            
            // 计算积分：每天10积分
            int points = totalDays * 10;
            
            // 更新用户积分
            userService.addUserPoints(userId, points);
            log.info("用户积分已更新: userId={}, points={}", userId, points);
            
            // 根据累计积分更新会员等级
            userService.updateMemberLevel(userId);
            log.info("用户会员等级已更新: userId={}", userId);
            
        } catch (Exception e) {
            log.error("处理订单完成消息失败: {}", message, e);
        }
    }
}
