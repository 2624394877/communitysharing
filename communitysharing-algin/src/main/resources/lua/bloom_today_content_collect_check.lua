local collectRedisKey = KEYS[1]
local collectContentIdAndCreatorId = ARGV[1]

local isExist = redis.call('EXISTS', collectRedisKey)
if isExist == 0 then
    redis.call("BF.ADD", collectRedisKey, '')
    redis.call("EXPIRE", collectRedisKey, 60*60*20)
end

return redis.call("BF.EXISTS",collectRedisKey, collectContentIdAndCreatorId) -- 返回1表示存在，0表示不存在