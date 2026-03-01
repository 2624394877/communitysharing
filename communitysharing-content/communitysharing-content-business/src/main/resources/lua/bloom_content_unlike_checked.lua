local key = KEYS[1] -- 操作的 Redis Key
local contentId = ARGV[1] -- 内容ID

local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

-- 校验该篇内容是否被点赞过(1 表示已经点赞，0 表示未点赞)
return redis.call('BF.EXISTS', key, contentId)