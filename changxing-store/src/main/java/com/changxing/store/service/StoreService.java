package com.changxing.store.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.store.entity.Store;
import com.changxing.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;

    public List<Store> getAllStores() {
        return storeMapper.selectList(null);
    }

    public List<Store> getActiveStores() {
        return storeMapper.selectList(
                new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1));
    }

    public List<Store> getStoresByProvince(String province) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, 1);
        if (province != null && !province.isEmpty() && !"all".equals(province)) {
            wrapper.eq(Store::getProvince, province);
        }
        return storeMapper.selectList(wrapper);
    }

    public Store getStoreById(Long id) {
        return storeMapper.selectById(id);
    }

    public Store createStore(Store store) {
        store.setStatus(1);
        storeMapper.insert(store);
        return store;
    }

    public Store updateStore(Long id, Store store) {
        Store existing = storeMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("门店不存在");
        }
        store.setId(id);
        storeMapper.updateById(store);
        return storeMapper.selectById(id);
    }

    public void updateStoreStatus(Long id, Integer status) {
        Store store = storeMapper.selectById(id);
        if (store != null) {
            store.setStatus(status);
            storeMapper.updateById(store);
        }
    }

    public void deleteStore(Long id) {
        storeMapper.deleteById(id);
    }
}
