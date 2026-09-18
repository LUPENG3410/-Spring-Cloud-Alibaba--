package com.changxing.booking.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.changxing.booking.dto.BookingCreateRequest;
import com.changxing.booking.dto.BookingDTO;
import com.changxing.booking.service.BookingService;
import com.changxing.common.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @SentinelResource(value = "createBooking", blockHandler = "createBookingBlock")
    public Result<BookingDTO> createBooking(
            @RequestBody BookingCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        BookingDTO booking = bookingService.createBooking(request, userId);
        return Result.success(booking);
    }

    public Result<BookingDTO> createBookingBlock(
            BookingCreateRequest request, HttpServletRequest httpRequest, BlockException ex) {
        return Result.fail(429, "下单请求过多，请稍后重试");
    }

    @GetMapping
    public Result<List<BookingDTO>> getUserBookings(HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        List<BookingDTO> bookings = bookingService.getUserBookings(userId);
        return Result.success(bookings);
    }

    @GetMapping("/{id}")
    public Result<BookingDTO> getBookingById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        BookingDTO booking = bookingService.getBookingById(id, userId);
        return Result.success(booking);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelBooking(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        bookingService.cancelBooking(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        bookingService.updateBookingStatus(id, status);
        return Result.success();
    }

    @GetMapping("/check-availability")
    @SentinelResource(value = "checkAvailability", blockHandler = "checkAvailabilityBlock")
    public Result<Boolean> checkAvailability(
            @RequestParam Long trimId,
            @RequestParam(required = false) String pickupProvince,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        boolean available = bookingService.checkAvailability(trimId, pickupProvince, startDate, endDate);
        return Result.success(available);
    }

    public Result<Boolean> checkAvailabilityBlock(
            Long trimId, String pickupProvince, String startDate, String endDate, BlockException ex) {
        return Result.fail(429, "查询请求过多，请稍后重试");
    }

    @GetMapping("/active-trim-ids")
    public Result<List<Long>> getActiveTrimIds() {
        List<Long> trimIds = bookingService.getActiveTrimIds();
        return Result.success(trimIds);
    }

    private Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            return 1L; // 默认测试用户
        }
        return Long.parseLong(userId);
    }
}
