package com.changxing.store;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.changxing.store.mapper")
public class ChangxingStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangxingStoreApplication.class, args);
    }

}
