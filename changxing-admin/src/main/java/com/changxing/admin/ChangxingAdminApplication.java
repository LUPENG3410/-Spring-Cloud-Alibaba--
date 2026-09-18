package com.changxing.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.changxing.admin.mapper")
@EnableFeignClients(basePackages = "com.changxing.common.feign")
@EnableScheduling
public class ChangxingAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangxingAdminApplication.class, args);
    }

}
