-- 原子预占：检查日期段是否与已有占用重叠，无重叠则写入
-- ZSET: score=endEpochDay, member="startEpochDay~endEpochDay"
-- KEYS[1] = occ:trim:{trimId}:{province}
-- ARGV[1] = startEpochDay
-- ARGV[2] = endEpochDay
-- ARGV[3] = member
-- 返回 1=预占成功, 0=日期段重叠
local start  = tonumber(ARGV[1])
local finish = tonumber(ARGV[2])
local members = redis.call('ZRANGEBYSCORE', KEYS[1], start, '+inf')
for _, m in ipairs(members) do
    local s, e = m:match("(%d+)~(%d+)")
    if tonumber(s) <= finish then
        return 0
    end
end
redis.call('ZADD', KEYS[1], finish, ARGV[3])
return 1