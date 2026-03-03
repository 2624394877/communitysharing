local key = KEYS[1]

for i = 1 ,#ARGV - 1 do -- 循环添加 最后一个位置放过期时间
    redis.call('BF.ADD', key, ARGV[i])
end

local expireTime = ARGV[#ARGV]
redis.call('EXPIRE', key, expireTime)
return 0