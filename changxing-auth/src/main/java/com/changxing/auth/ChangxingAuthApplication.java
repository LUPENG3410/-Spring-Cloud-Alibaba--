package com.changxing.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.changxing.auth", "com.changxing.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.changxing.common.feign")
public class ChangxingAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangxingAuthApplication.class, args);
    }
}
