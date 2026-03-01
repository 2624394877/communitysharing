-- 添加关注关系并设置过期时间
local key = KEYS[1] -- 关注关系 key
local followUserId = ARGV[1] -- 关注者 id
local timestamp = ARGV[2] -- 时间戳
local expireTime = ARGV[3] -- 过期时间

-- 添加 ZADD 关系
redis.call('ZADD', key, timestamp, followUserId)
-- 设置过期时间
redis.call('EXPIRE', key, expireTime)
return 0 -- 返回成功