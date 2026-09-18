package com.changxing.store.controller;

import com.changxing.common.dto.Result;
import com.changxing.store.entity.Store;
import com.changxing.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public Result<List<Store>> getStores(
            @RequestParam(value = "province", required = false) String province) {
        if (province != null) {
            return Result.success(storeService.getStoresByProvince(province));
        }
        return Result.success(storeService.getActiveStores());
    }

    @GetMapping("/{id}")
    public Result<Store> getStoreById(@PathVariable Long id) {
        return Result.success(storeService.getStoreById(id));
    }

    @PostMapping
    public Result<Store> createStore(@RequestBody Store store) {
        return Result.success(storeService.createStore(store));
    }

    @PutMapping("/{id}")
    public Result<Store> updateStore(@PathVariable Long id, @RequestBody Store store) {
        return Result.success(storeService.updateStore(id, store));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStoreStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        storeService.updateStoreStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return Result.success();
    }
}
