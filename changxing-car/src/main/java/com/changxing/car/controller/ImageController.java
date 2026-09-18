package com.changxing.car.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * 代理访问MinIO图片
     * 前端调用: GET /images/{objectName}
     * objectName示例: cars/exterior/xxx.jpg
     */
    @GetMapping("/{objectName:.+}")
    public void getImage(@PathVariable String objectName, HttpServletResponse response) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build())) {

            response.setContentType("image/jpeg");
            response.getOutputStream().write(stream.readAllBytes());
            response.getOutputStream().flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
