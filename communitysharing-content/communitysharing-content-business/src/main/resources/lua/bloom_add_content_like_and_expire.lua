-- 操作的 Key
local key = KEYS[1]
local contentId = ARGV[1] -- 笔记ID
local expireTime = ARGV[2] -- 过期时间（秒）

redis.call("BF.ADD", key, contentId)
-- 设置过期时间
redis.call("EXPIRE", key, expireTime)
return 0
