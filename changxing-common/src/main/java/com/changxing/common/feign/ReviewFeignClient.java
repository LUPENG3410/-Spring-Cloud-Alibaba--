package com.changxing.common.feign;

import com.changxing.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "changxing-review", path = "/reviews")
public interface ReviewFeignClient {

    @PostMapping
    Result<Map<String, Object>> createReview(@RequestBody Map<String, Object> request,
                                             @RequestHeader("X-User-Id") Long userId);

    @GetMapping("/trim/{trimId}")
    Result<List<Map<String, Object>>> getReviewsByTrimId(@PathVariable("trimId") Long trimId);
}
