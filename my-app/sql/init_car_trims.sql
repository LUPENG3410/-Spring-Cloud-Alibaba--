-- 畅行租车 - 车款初始数据 (基于前端 carDatabase.ts)

INSERT INTO car_trims (brand, series_name, name, type, year, image, price, rental_price, seats, fuel, transmission, engine, horsepower, torque, displacement, acceleration, top_speed, fuel_consumption, wheelbase, length, width, height, trunk_volume, weight, fuel_type, fuel_grade, fuel_tank_capacity, doors, reversing_camera, auto_hold, driver_assist, smart_connect, electric_seat, seat_function, images, interior_images, features, descriptions) VALUES
-- 大众 朗逸
('大众', '朗逸', '朗逸 2024款 1.5L 自动舒适版', 'sedan', 2024, 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop', 12.80, 128, 5, '92号汽油', '自动', '1.5L EA211', 113, 145, '1.5L', '11.2s', '190km/h', '5.9L/100km', '2688mm', '4678mm', '1806mm', '1474mm', '510L', '1280kg', '汽油', '92号', '55L', '4门', 1, 0, '无', '无', 0, '无',
'["https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["倒车影像","定速巡航","自动空调","多功能方向盘","蓝牙连接","后排出风口"]',
'["朗逸作为大众旗下经典家轿，以可靠品质和经济油耗著称","宽敞的乘坐空间和510L超大后备箱，满足家庭出行需求","配备ESP车身稳定系统、倒车影像等实用配置"]'),

('大众', '朗逸', '朗逸 2024款 1.5L 自动豪华版', 'sedan', 2024, 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop', 14.80, 148, 5, '92号汽油', '自动', '1.5L EA211', 113, 145, '1.5L', '11.2s', '190km/h', '5.9L/100km', '2688mm', '4678mm', '1806mm', '1474mm', '510L', '1280kg', '汽油', '92号', '55L', '4门', 1, 0, '无', '无', 0, '无',
'["https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop"]',
'["倒车影像","定速巡航","自动空调","多功能方向盘","蓝牙连接","后排出风口","全景天窗","无钥匙进入"]',
'["朗逸豪华版增加全景天窗、无钥匙进入等高级配置","EA211发动机成熟可靠，油耗经济"]'),

-- 大众 速腾
('大众', '速腾', '速腾 2024款 1.4T 自动舒适版', 'sedan', 2024, 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800&h=500&fit=crop', 11.80, 118, 5, '92号汽油', '自动', '1.4T EA211', 150, 250, '1.4T', '9.3s', '200km/h', '5.8L/100km', '2731mm', '4753mm', '1800mm', '1462mm', '553L', '1350kg', '汽油', '92号', '55L', '4门', 0, 0, '无', '无', 0, '无',
'["https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["ESP车身稳定","倒车雷达","定速巡航","多功能方向盘","蓝牙","后排出风口"]',
'["速腾是A+级轿车市场的热门选择","1.4T涡轮增压发动机+7速双离合，动力充沛","2731mm轴距带来同级领先的乘坐空间"]'),

-- 丰田 凯美瑞
('丰田', '凯美瑞', '凯美瑞 2024款 2.0G 豪华版', 'sedan', 2024, 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800&h=500&fit=crop', 19.80, 198, 5, '92号汽油', '自动', '2.0L Dynamic Force', 178, 210, '2.0L', '9.1s', '210km/h', '6.0L/100km', '2825mm', '4900mm', '1840mm', '1455mm', '524L', '1430kg', '汽油', '92号', '60L', '4门', 1, 0, 'L2', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1617531653342-5a1f8a517b8e?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop"]',
'["全景天窗","车道偏离预警","自适应巡航","预碰撞安全","智能互联","JBL音响"]',
'["凯美瑞基于TNGA架构打造，操控与舒适兼备","2.0L Dynamic Force发动机热效率达40%，油耗更低","Toyota Safety Sense智行安全系统，全方位守护"]'),

-- 丰田 汉兰达
('丰田', '汉兰达', '汉兰达 2024款 2.5L HEV 四驱豪华版', 'suv', 2024, 'https://images.unsplash.com/photo-1594611625027-0e40e7ab6e54?w=800&h=500&fit=crop', 29.80, 298, 7, '92号汽油', '自动', '2.5L HEV混动', 250, 238, '2.5L', '8.4s', '180km/h', '5.3L/100km', '2850mm', '4965mm', '1930mm', '1750mm', '456L', '1870kg', '汽油', '92号', '65L', '4门', 1, 0, 'L2', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1594611625027-0e40e7ab6e54?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1519245659620-e859806a8d7b?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop"]',
'["四驱系统","第三排座椅","电动后备箱","丰田智行安全","JBL音响","车道保持"]',
'["汉兰达是国内7座SUV市场的常青树，口碑极佳","2.5L混合动力系统，油耗低至5.3L/100km","E-Four电子四驱系统，稳定可靠"]'),

-- 本田 CR-V
('本田', 'CR-V', 'CR-V 2024款 240TURBO 四驱豪华版', 'suv', 2024, 'https://images.unsplash.com/photo-1568844293986-8d0400f4745b?w=800&h=500&fit=crop', 22.80, 228, 5, '92号汽油', '自动', '1.5T VTEC Turbo', 193, 243, '1.5T', '9.5s', '200km/h', '6.8L/100km', '2700mm', '4703mm', '1866mm', '1680mm', '589L', '1545kg', '汽油', '92号', '53L', '4门', 1, 0, 'L2', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1568844293986-8d0400f4745b?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1519245659620-e859806a8d7b?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["四驱系统","全景天窗","360全景影像","HUD抬头显示","BOSE音响","电动尾门"]',
'["CR-V是全球最畅销的SUV之一，空间表现出色","1.5T涡轮增压+CVT动力组合，动力充沛油耗经济","Honda SENSING安全超感，L2级辅助驾驶"]'),

-- 别克 GL8
('别克', 'GL8', 'GL8 2024款 ES陆尊 2.0T 豪华型', 'mpv', 2024, 'https://images.unsplash.com/photo-1549317661-bd32c8ce0afa?w=800&h=500&fit=crop', 35.80, 358, 7, '95号汽油', '自动', '2.0T Ecotec', 237, 350, '2.0T', '9.8s', '200km/h', '8.5L/100km', '3088mm', '5238mm', '1878mm', '1776mm', '521L', '1880kg', '汽油', '95号', '66L', '4门', 1, 0, '无', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1549317661-bd32c8ce0afa?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["航空座椅","电动侧滑门","后排娱乐屏","车载冰箱","Bose音响","无线充电"]',
'["GL8是国内商务MPV标杆，接待首选车型","第二排航空座椅带腿托、按摩、加热功能","3088mm超长轴距，三排空间同样宽裕"]'),

-- 宝马 3系
('宝马', '3系', '宝马3系 2024款 325Li M运动套装', 'luxury', 2024, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=500&fit=crop', 39.80, 398, 5, '95号汽油', '自动', '2.0T B48', 184, 300, '2.0T', '7.3s', '240km/h', '6.9L/100km', '2966mm', '4838mm', '1827mm', '1454mm', '480L', '1545kg', '汽油', '95号', '59L', '4门', 1, 1, 'L2', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1556189250-72ba954cfc2b?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["M运动套件","哈曼卡顿音响","全液晶仪表","座椅加热","自动泊车","CarPlay"]',
'["宝马3系是豪华运动轿车的标杆，操控乐趣十足","B48 2.0T发动机+ZF 8AT变速箱，黄金动力总成","50:50前后配重比，带来极致驾驶体验"]'),

-- 奔驰 C级
('奔驰', 'C级', '奔驰C级 2024款 C 260 L 运动版', 'luxury', 2024, 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800&h=500&fit=crop', 42.80, 428, 5, '95号汽油', '自动', '1.5T+48V M254', 204, 300, '1.5T', '7.8s', '235km/h', '6.5L/100km', '2954mm', '4882mm', '1820mm', '1461mm', '475L', '1550kg', '汽油', '95号', '66L', '4门', 1, 1, 'L2', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1617531653342-5a1f8a517b8e?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["64色氛围灯","柏林之声","智能泊车","MBUX系统","无线充电","座椅记忆"]',
'["全新C级采用家族最新设计语言，豪华感大幅提升","11.9英寸中控竖屏+MBUX智能人机交互系统","64色主动式氛围灯，营造沉浸式座舱体验"]'),

-- 保时捷 718
('保时捷', '718', '保时捷718 2024款 Boxster 2.0T', 'sports', 2024, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 128.00, 1280, 2, '98号汽油', '自动', '2.0T Flat-4', 300, 380, '2.0T', '4.7s', '275km/h', '8.1L/100km', '2475mm', '4379mm', '1801mm', '1295mm', '150L', '1335kg', '汽油', '98号', '54L', '2门', 1, 0, '无', '无', 0, '无',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop"]',
'["PDK变速箱","Sport Chrono","运动排气","碳纤维内饰","SC组件","PDLS大灯"]',
'["保时捷718是入门级跑车的标杆，驾驶乐趣极致","中置水平对置发动机，完美的50:50配重","Sport Chrono组件，0-100km/h仅需4.7秒"]'),

-- 日产 轩逸
('日产', '轩逸', '轩逸 2024款 1.6L CVT 智驾版', 'sedan', 2024, 'https://images.unsplash.com/photo-1590362891991-f776e747a588?w=800&h=500&fit=crop', 9.90, 99, 5, '92号汽油', '自动', '1.6L HR16', 122, 155, '1.6L', '12.0s', '185km/h', '5.2L/100km', '2712mm', '4641mm', '1815mm', '1450mm', '560L', '1230kg', '汽油', '92号', '50L', '4门', 1, 0, '无', '基础互联', 0, '无',
'["https://images.unsplash.com/photo-1590362891991-f776e747a588?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop"]',
'["智能互联","主动刹车","疲劳提醒","倒车影像","多功能方向盘","后排出风口"]',
'["轩逸是国内家轿销量王，以舒适性和经济性著称","日产Multi-Layer人体工学座椅，移动大沙发","HR16发动机+CVT，百公里油耗仅5.2L"]'),

-- 奥迪 A4L
('奥迪', 'A4L', '奥迪A4L 2024款 40 TFSI 豪华致雅型', 'luxury', 2024, 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=500&fit=crop', 36.80, 368, 5, '95号汽油', '自动', '2.0T EA888', 190, 320, '2.0T', '7.6s', '240km/h', '6.8L/100km', '2908mm', '4858mm', '1847mm', '1439mm', '460L', '1585kg', '汽油', '95号', '58L', '4门', 1, 1, 'L2', '智能互联', 1, '座椅加热',
'["https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1617531653342-5a1f8a517b8e?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop"]',
'["矩阵大灯","B&O音响","虚拟座舱","MMI系统","座椅加热","30色氛围灯"]',
'["奥迪A4L是豪华中型轿车的标杆之一","EA888 2.0T发动机+7速S tronic双离合变速箱","虚拟座舱+MMI触控系统，科技感十足"]'),

-- 哈弗 H6
('哈弗', 'H6', '哈弗H6 2024款 1.5T 自动冠军版', 'suv', 2024, 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800&h=500&fit=crop', 15.80, 158, 5, '92号汽油', '自动', '1.5T GW4B15A', 169, 285, '1.5T', '10.2s', '190km/h', '6.8L/100km', '2738mm', '4703mm', '1886mm', '1730mm', '600L', '1555kg', '汽油', '92号', '55L', '4门', 1, 0, 'L2', '智能互联', 0, '无',
'["https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800&h=500&fit=crop","https://images.unsplash.com/photo-1519245659620-e859806a8d7b?w=800&h=500&fit=crop"]',
'["https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop"]',
'["全景天窗","360全景影像","智能语音","自动泊车","L2辅助驾驶","无线充电"]',
'["哈弗H6连续多年蝉联SUV销量冠军","1.5T发动机+7DCT湿式双离合，动力平顺","600L超大后备箱，满足全家出行需求"]');
