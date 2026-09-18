-- 补充插入尊享服务和尊享百万升级版
INSERT INTO insurance_products (code, name, description, daily_price, total_price,
    vehicle_loss_deductible, vehicle_loss_coverage_rate, tire_loss_covered,
    third_party_limit, third_party_medical_covered, third_party_medical_limit,
    driver_loss_limit, stop_fee_covered, single_accident_no_doc_limit, status, sort_order) VALUES
('premium', '尊享服务', '车辆损失无免赔，轮胎损失保障，30万三者险', 30.00, 30.00,
    0.00, 100.00, 1, 300000.00, 0, 0.00, 50000.00, 1, 5000.00, 1, 2),
('premium_plus', '尊享百万升级版服务', '车辆损失无免赔，100万三者险，10万医保外医疗，10万驾驶员保障', 50.00, 50.00,
    0.00, 100.00, 1, 1000000.00, 1, 100000.00, 100000.00, 1, 5000.00, 1, 3);
