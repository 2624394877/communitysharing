local key = KEYS[1]
local userId = ARGV[1]
local expireTime = ARGV[2]

redis.call('BF.ADD', key, userId)

redis.call('EXPIRE', key, expireTime)
return 0