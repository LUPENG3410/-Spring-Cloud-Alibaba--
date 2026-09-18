package com.changxing.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.common.dto.Result;
import com.changxing.common.exception.BusinessException;
import com.changxing.common.feign.UserFeignClient;
import com.changxing.review.dto.ReviewCreateRequest;
import com.changxing.review.dto.ReviewDTO;
import com.changxing.review.entity.Review;
import com.changxing.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final UserFeignClient userFeignClient;

    public ReviewDTO createReview(ReviewCreateRequest request, Long userId) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }
        if (request.getTrimId() == null) {
            throw new BusinessException("车款ID不能为空");
        }

        // 检查是否已评价过
        LambdaQueryWrapper<Review> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Review::getUserId, userId)
                .eq(Review::getTrimId, request.getTrimId());
        if (reviewMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("您已评价过该车款");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setTrimId(request.getTrimId());
        review.setBookingId(request.getBookingId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCarDays(request.getCarDays());
        review.setCreatedAt(LocalDateTime.now());

        reviewMapper.insert(review);

        return convertToDTO(review);
    }

    public List<ReviewDTO> getReviewsByTrimId(Long trimId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getTrimId, trimId)
                .orderByDesc(Review::getCreatedAt);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setTrimId(review.getTrimId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setCarDays(review.getCarDays());
        dto.setCreatedAt(review.getCreatedAt());

        // 查询用户信息
        try {
            Result<Map<String, Object>> userResult = userFeignClient.getUserById(review.getUserId());
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                dto.setUserName((String) userResult.getData().get("name"));
                String name = dto.getUserName();
                dto.setAvatar(name != null && !name.isEmpty() ? name.substring(0, 1) : "用");
            }
        } catch (Exception e) {
            dto.setUserName("用户" + review.getUserId());
            dto.setAvatar("用");
        }

        return dto;
    }
}
