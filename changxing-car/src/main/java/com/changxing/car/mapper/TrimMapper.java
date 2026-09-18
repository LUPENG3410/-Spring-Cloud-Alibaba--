package com.changxing.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.changxing.car.dto.CarDTO;
import com.changxing.car.dto.TrimDetailDTO;
import com.changxing.car.entity.CarTrim;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface TrimMapper extends BaseMapper<CarTrim> {

    @Select("""
            <script>
            SELECT DISTINCT
                t.id,
                t.name,
                t.brand,
                t.type,
                t.image,
                t.price,
                t.rental_price AS rentalPrice,
                t.seats,
                t.fuel_grade AS fuel,
                t.transmission,
                c.province,
                s.province AS homeProvince,
                CASE WHEN c.province != s.province THEN 1 ELSE 0 END AS isHitch,
                1 AS available,
                t.features
            FROM car_trims t
            INNER JOIN cars c
                ON t.id = c.trim_id
                AND c.status = 'available'
                AND c.deleted_at IS NULL
            INNER JOIN stores s
                ON c.store_id = s.id
            WHERE t.status = 1
            <if test="type != null and type != ''">
                AND t.type = #{type}
            </if>
            <if test="province != null and province != ''">
                AND c.province = #{province}
            </if>
            <if test="keyword != null and keyword != ''">
                AND (t.brand LIKE CONCAT('%',#{keyword},'%') OR t.name LIKE CONCAT('%',#{keyword},'%'))
            </if>
            ORDER BY t.rental_price ASC
            </script>
            """)
    List<CarDTO> listHasStockTrim(
            @Param("type") String type,
            @Param("province") String province,
            @Param("keyword") String keyword
    );

    @Select("""
            <script>
            SELECT DISTINCT
                t.id,
                t.name,
                t.brand,
                t.type,
                t.image,
                t.price,
                t.rental_price AS rentalPrice,
                t.seats,
                t.fuel_grade AS fuel,
                t.transmission,
                c.province AS currentProvince,
                s.province AS homeProvince,
                CASE WHEN c.province != s.province THEN 1 ELSE 0 END AS isHitch,
                1 AS available,
                t.features
            FROM car_trims t
            INNER JOIN cars c
                ON t.id = c.trim_id
                AND c.status = 'available'
                AND c.deleted_at IS NULL
            INNER JOIN stores s
                ON c.store_id = s.id
            WHERE t.status = 1
            AND c.province != s.province
            ORDER BY t.rental_price ASC
            </script>
            """)
    List<CarDTO> listHitchCars();

    @Select("""
            SELECT
                t.id,
                t.name,
                t.brand,
                t.series_name AS seriesName,
                t.type,
                t.year,
                t.image,
                t.price,
                t.rental_price AS rentalPrice,
                t.seats,
                t.fuel,
                t.transmission,
                t.features AS featuresStr,
                t.engine,
                t.horsepower,
                t.torque,
                t.displacement,
                t.acceleration,
                t.top_speed AS topSpeed,
                t.fuel_consumption AS fuelConsumption,
                t.wheelbase,
                t.length,
                t.width,
                t.height,
                t.trunk_volume AS trunkVolume,
                t.weight,
                t.fuel_type AS fuelType,
                t.fuel_grade AS fuelGrade,
                t.fuel_tank_capacity AS fuelTankCapacity,
                t.doors,
                t.reversing_camera AS reversingCamera,
                t.auto_hold AS autoHold,
                t.driver_assist AS driverAssist,
                t.smart_connect AS smartConnect,
                t.electric_seat AS electricSeat,
                t.seat_function AS seatFunction,
                t.images AS imagesStr,
                t.interior_images AS interiorImagesStr,
                t.descriptions AS descriptionsStr
            FROM car_trims t
            WHERE t.id = #{trimId} AND t.status = 1
            """)
    TrimDetailDTO getTrimDetail(@Param("trimId") Long trimId);

    @Select("""
            SELECT
                t.id AS trimId,
                t.name AS name,
                t.brand AS brand,
                t.image AS image,
                t.price AS price,
                t.rental_price AS rentalPrice,
                t.type AS type,
                t.seats AS seats,
                t.fuel_grade AS fuel,
                t.transmission AS transmission,
                COUNT(b.id) AS orderCount
            FROM car_trims t
            LEFT JOIN bookings b ON t.id = b.trim_id AND b.status = 'completed'
            WHERE t.status = 1
            GROUP BY t.id, t.name, t.brand, t.image, t.price, t.rental_price, t.type, t.seats, t.fuel_grade, t.transmission
            ORDER BY orderCount DESC
            LIMIT 4
            """)
    List<TrimDetailDTO> listHotTrims();
}