package com.changxing.car;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.changxing.car.mapper")
@EnableFeignClients(basePackages = "com.changxing.common.feign")
public class CarApplication {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public static void main(String[] args) {
//        SpringApplication.run(CarApplication.class, args);
        ConfigurableApplicationContext ctx = SpringApplication.run(CarApplication.class, args);
        String appName = ctx.getEnvironment().getProperty("spring.application.name");
        System.out.println("========= 当前生效服务名：" + appName + " =========");
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                System.out.println("Bucket [" + bucket + "] 创建成功");
            }
        } catch (Exception e) {
            System.err.println("Bucket初始化失败: " + e.getMessage());
        }
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}