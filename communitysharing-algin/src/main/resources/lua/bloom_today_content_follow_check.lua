local followKey = KEYS[1]
local userId = ARGV[1]

local exits = redis.call('EXISTS', followKey)
if exits == 0 then
    redis.call('BF.ADD',followKey,'')
    redis.call('EXPIRE',followKey,60*60*20)
end

return redis.call('BF.EXISTS',followKey,userId)