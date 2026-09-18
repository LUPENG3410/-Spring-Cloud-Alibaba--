package com.changxing.common.feign;

import com.changxing.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "changxing-booking", path = "/bookings")
public interface BookingFeignClient {

    @GetMapping("/active-trim-ids")
    Result<List<Long>> getActiveTrimIds();

    @PutMapping("/{id}/status")
    Result<Void> updateBookingStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}
