package com.changxing.admin.controller;

import com.changxing.admin.dto.*;
import com.changxing.admin.service.AdminDashboardService;
import com.changxing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public Result<AdminStatsDTO> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/bookings")
    public Result<List<BookingDTO>> getBookings() {
        return Result.success(dashboardService.getBookings());
    }

    @PutMapping("/bookings/{id}/status")
    public Result<Void> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        dashboardService.updateBookingStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/users")
    public Result<List<UserDTO>> getUsers() {
        return Result.success(dashboardService.getUsers());
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        dashboardService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/cars")
    public Result<List<CarDTO>> getCars() {
        return Result.success(dashboardService.getCars());
    }

    @PostMapping("/cars")
    public Result<CarDTO> createCar(@RequestBody CreateCarRequest request) {
        return Result.success(dashboardService.createCar(request));
    }

    @PostMapping("/cars/inventory")
    public Result<CarDTO> createCarInventory(@RequestBody CreateCarInventoryRequest request) {
        return Result.success(dashboardService.createCarInventory(request));
    }

    @PutMapping("/cars/{id}/status")
    public Result<Void> updateCarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        dashboardService.updateCarStatus(id, body.get("status"));
        return Result.success();
    }

    @PutMapping("/cars/{id}")
    public Result<Void> updateCar(@PathVariable Long id, @RequestBody UpdateCarRequest request) {
        dashboardService.updateCar(id, request);
        return Result.success();
    }

    @DeleteMapping("/cars/{id}")
    public Result<Void> deleteCar(@PathVariable Long id) {
        dashboardService.deleteCar(id);
        return Result.success();
    }

    @GetMapping("/trims")
    public Result<List<TrimDTO>> getTrims() {
        return Result.success(dashboardService.getTrims());
    }

    @PostMapping("/trims")
    public Result<TrimDTO> createTrim(@RequestBody CreateTrimRequest request) {
        return Result.success(dashboardService.createTrim(request));
    }

    @PutMapping("/trims/{id}/status")
    public Result<Void> updateTrimStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        dashboardService.updateTrimStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/trims/{id}")
    public Result<Void> deleteTrim(@PathVariable Long id) {
        dashboardService.deleteTrim(id);
        return Result.success();
    }

    @GetMapping("/stores")
    public Result<List<StoreDTO>> getStores() {
        return Result.success(dashboardService.getStores());
    }

    @PutMapping("/stores/{id}/status")
    public Result<Void> updateStoreStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        dashboardService.updateStoreStatus(id, body.get("status"));
        return Result.success();
    }

    @PostMapping("/stores")
    public Result<StoreDTO> createStore(@RequestBody com.changxing.admin.entity.Store store) {
        com.changxing.admin.entity.Store created = dashboardService.createStore(store);
        StoreDTO dto = new StoreDTO();
        dto.setId(created.getId());
        dto.setName(created.getName());
        dto.setProvince(created.getProvince());
        dto.setCity(created.getCity());
        dto.setAddress(created.getAddress());
        dto.setPhone(created.getPhone());
        dto.setLat(created.getLat());
        dto.setLng(created.getLng());
        dto.setHours(created.getHours());
        dto.setStatus(created.getStatus() != null && created.getStatus() == 1 ? "active" : "closed");
        return Result.success(dto);
    }

    @PutMapping("/stores/{id}")
    public Result<StoreDTO> updateStore(@PathVariable Long id, @RequestBody com.changxing.admin.entity.Store store) {
        com.changxing.admin.entity.Store updated = dashboardService.updateStore(id, store);
        StoreDTO dto = new StoreDTO();
        dto.setId(updated.getId());
        dto.setName(updated.getName());
        dto.setProvince(updated.getProvince());
        dto.setCity(updated.getCity());
        dto.setAddress(updated.getAddress());
        dto.setPhone(updated.getPhone());
        dto.setLat(updated.getLat());
        dto.setLng(updated.getLng());
        dto.setHours(updated.getHours());
        dto.setStatus(updated.getStatus() != null && updated.getStatus() == 1 ? "active" : "closed");
        return Result.success(dto);
    }

    @DeleteMapping("/stores/{id}")
    public Result<Void> deleteStore(@PathVariable Long id) {
        dashboardService.deleteStore(id);
        return Result.success();
    }
}
