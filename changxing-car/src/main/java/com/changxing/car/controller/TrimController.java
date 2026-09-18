package com.changxing.car.controller;

import com.changxing.car.dto.CarDTO;
import com.changxing.car.dto.TrimDetailDTO;
import com.changxing.car.service.CarService;
import com.changxing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class TrimController {

    private final CarService carService;

    /**
     * 获取热门车型（按已完成订单数排名前4）
     */
    @GetMapping("/hot")
    public Result<List<TrimDetailDTO>> hotTrims() {
        return Result.success(carService.getHotTrims());
    }

    /**
     * 获取有库存车辆列表
     * @param type 车型 sedan/suv
     * @param province 省份筛选
     * @param keyword 名称/品牌搜索
     */
    @GetMapping
    public Result<List<CarDTO>> carList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String keyword
    ) {
        List<CarDTO> data = carService.getHasStockCarList(type, province, keyword);
        return Result.success(data);
    }

    /**
     * 获取顺风车列表（当前所在省份 != 原籍省份）
     */
    @GetMapping("/hitch")
    public Result<List<CarDTO>> hitchCars() {
        List<CarDTO> data = carService.getHitchCarList();
        return Result.success(data);
    }

    /**
     * 异地还车时更新车辆当前省份
     */
    @PutMapping("/province")
    public Result<Void> updateCarProvince(
            @RequestParam Long trimId,
            @RequestParam String fromProvince,
            @RequestParam String toProvince) {
        carService.updateCarProvince(trimId, fromProvince, toProvince);
        return Result.success();
    }

    /**
     * 获取车款详情
     * @param trimId 车款ID
     */
    @GetMapping("/{trimId}")
    public Result<TrimDetailDTO> getTrimDetail(@PathVariable Long trimId) {
        TrimDetailDTO data = carService.getTrimDetail(trimId);
        if (data == null) {
            return Result.fail(404, "车款不存在");
        }
        return Result.success(data);
    }
}