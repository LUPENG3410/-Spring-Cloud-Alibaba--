package com.changxing.review.controller;

import com.changxing.common.dto.Result;
import com.changxing.review.dto.ReviewCreateRequest;
import com.changxing.review.dto.ReviewDTO;
import com.changxing.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<ReviewDTO> createReview(
            @RequestBody ReviewCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        ReviewDTO review = reviewService.createReview(request, userId);
        return Result.success(review);
    }

    @GetMapping("/trim/{trimId}")
    public Result<List<ReviewDTO>> getReviewsByTrimId(@PathVariable Long trimId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByTrimId(trimId);
        return Result.success(reviews);
    }

    private Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            return 1L;
        }
        return Long.parseLong(userId);
    }
}
