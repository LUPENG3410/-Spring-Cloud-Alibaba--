package com.changxing.admin.controller;

import com.changxing.common.dto.Result;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;

    private static final String BUCKET_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"AWS": ["*"]},
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }]
            }
            """;

    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "exterior") String type) {

        log.info("收到上传请求: fileName={}, size={}, type={}", file.getOriginalFilename(), file.getSize(), type);

        try {
            ensureBucketExists();

            String ext = getExtension(file.getOriginalFilename());
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String objectName = "cars/" + type + "/" + datePath + "/" + UUID.randomUUID() + ext;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String url = endpoint + "/" + bucket + "/" + objectName;
            log.info("上传成功: url={}", url);
            return Result.success(Map.of(
                    "url", url,
                    "fileName", objectName,
                    "size", file.getSize()
            ));
        } catch (Exception e) {
            log.error("上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestBody Map<String, String> body) {
        try {
            String url = body.get("url");
            if (url == null || url.isBlank()) return Result.fail("缺少url参数");

            String objectName = url.replace(endpoint + "/" + bucket + "/", "");
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            return Result.success();
        } catch (Exception e) {
            log.error("删除失败", e);
            return Result.fail("文件删除失败: " + e.getMessage());
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("创建bucket: {}", bucket);
        }
        setBucketPublicRead();
    }

    private void setBucketPublicRead() {
        try {
            String policy = String.format(BUCKET_POLICY, bucket);
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(policy)
                    .build());
            log.info("设置bucket公开读取策略: {}", bucket);
        } catch (Exception e) {
            log.warn("设置bucket策略失败(可能已设置): {}", e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
