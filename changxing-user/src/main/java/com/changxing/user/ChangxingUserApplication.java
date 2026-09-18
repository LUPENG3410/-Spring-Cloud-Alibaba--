package com.changxing.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.changxing.user.mapper")
public class ChangxingUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangxingUserApplication.class, args);
    }
}
