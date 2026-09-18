package com.changxing.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.changxing.common.feign")
@EnableScheduling
@MapperScan("com.changxing.booking.mapper")
public class ChangxingBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangxingBookingApplication.class, args);
    }

}
