-- 顺风车测试数据（按车牌号定位，可靠）
-- 把几辆车的当前省份改为异地，模拟异地还车后需要开回原籍

-- 北京门店的车被还到了宁夏
UPDATE cars SET province = '宁夏', city = '银川' WHERE plate_number = '京A10005';
UPDATE cars SET province = '宁夏', city = '银川' WHERE plate_number = '京B20002';

-- 上海门店的车被还到了广东
UPDATE cars SET province = '广东', city = '广州' WHERE plate_number = '沪A40004';
UPDATE cars SET province = '广东', city = '广州' WHERE plate_number = '沪B50002';

-- 广东门店的车被还到了四川
UPDATE cars SET province = '四川', city = '成都' WHERE plate_number = '粤B80001';
UPDATE cars SET province = '四川', city = '成都' WHERE plate_number = '粤C90002';

-- 四川门店的车被还到了浙江
UPDATE cars SET province = '浙江', city = '杭州' WHERE plate_number = '川A11002';

-- 宁夏门店的车被还到了北京
UPDATE cars SET province = '北京', city = '北京' WHERE plate_number = '宁A15001';
UPDATE cars SET province = '北京', city = '北京' WHERE plate_number = '宁B16002';

-- 浙江门店的车被还到了山东
UPDATE cars SET province = '山东', city = '济南' WHERE plate_number = '浙A13002';
