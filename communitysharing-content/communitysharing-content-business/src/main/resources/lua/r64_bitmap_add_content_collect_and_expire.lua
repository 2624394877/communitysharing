local key = KEYS[1]
local contentId = ARGV[1]
local expireTime = ARGV[2]
redis.call('R64.SETBIT',key,contentId, 1)
redis.call('EXPIRE',key,expireTime)
return 0 -- 返回0表示成功，1表示失败（内容已存在）