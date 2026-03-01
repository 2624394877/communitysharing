local key = KEYS[1]
local contentIdAndCreatorId = ARGV[1]

local exists = redis.call("EXISTS",key)
if exists == 0 then
    redis.call("BF.ADD",key,'') -- 如果过滤器没有，就创建一个
    redis.call("EXPIRE",key,60*60*20) -- 为这个过滤器设置过期时间：一天
end

return redis.call("BF.EXISTS",key,contentIdAndCreatorId) -- 判断笔记是否有没有在过滤器中