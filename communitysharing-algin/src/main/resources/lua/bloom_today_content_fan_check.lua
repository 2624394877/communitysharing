local fanKey = KEYS[1]
local userId = ARGV[1]

local exists = redis.call('EXISTS', fanKey)
if exists == 0 then
  redis.call('BF.ADD',fanKey,'')
  redis.call('EXPIRE',fanKey,60*60*20)
end

return redis.call('BF.EXISTS',fanKey,userId)