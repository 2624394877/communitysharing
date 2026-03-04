local key = KEYS[1]

-- #ARGV：

-- 遍历ARGV集合，将每个元素添加到布隆过滤器中
for i = 1, #ARGV-1 do
    redis.call("R64.SETBIT", key, ARGV[i], 1)
end

-- 将最后一个元素作为过期时间，设置布隆过滤器的过期时间
local expireTime = ARGV[#ARGV] -- 最后一个元素
redis.call("EXPIRE", key, expireTime)
return 0 -- 返回0表示成功执行脚本