package com.changxing.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.changxing.review.mapper")
@EnableFeignClients(basePackages = "com.changxing.common.feign")
public class ChangxingReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangxingReviewApplication.class, args);
    }

}
