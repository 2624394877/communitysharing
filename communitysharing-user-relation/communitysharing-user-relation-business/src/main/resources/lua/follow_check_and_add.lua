-- 校验并添加关注关系
local key = KEYS[1] -- 关注关系 key
local followUserId = ARGV[1] -- 关注者 id
local timestamp = ARGV[2] -- 时间戳

-- 使用 EXISTS 命令检查 ZSET 是否存在
local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

-- 使用 ZCARD 校验关注人数是否上限（是否达到 1000）
local count = redis.call('ZCARD', key)
if count >= 1000 then
    return -2
end

-- 使用 ZSCORE 命令检查是否已经关注
local score = redis.call('ZSCORE', key, followUserId)
if score then
    return -3
end

-- 使用 ZADD 命令添加关注关系
redis.call('ZADD', key, timestamp, followUserId)
return 0 -- 成功