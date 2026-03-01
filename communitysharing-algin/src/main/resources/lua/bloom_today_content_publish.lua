local userPublishKey = KEYS[1]
local userId = ARGV[1]

local exists = redis.call('EXISTS', userPublishKey)
if exists == 0 then
    redis.call("BF.ADD", userPublishKey, '')
    redis.call("EXPIRE", userPublishKey, 60*60*20)
end

return redis.call('BF.EXISTS', userPublishKey, userId)