local key = KEYS[1] -- 操作的 Redis Key
local contentId = ARGV[1] -- 内容ID

local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

return redis.call("BF.EXISTS", key, contentId)