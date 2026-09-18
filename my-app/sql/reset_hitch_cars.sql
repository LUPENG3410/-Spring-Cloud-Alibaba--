-- 清理顺风车假数据，恢复车辆到原籍门店的省份
UPDATE cars c JOIN stores s ON c.store_id = s.id SET c.province = s.province, c.city = s.city
WHERE c.province != s.province;
