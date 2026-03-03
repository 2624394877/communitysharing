local key = KEYS[1]
local userId = ARGV[1]

local isExists = redis.call('EXISTS', key)
if isExists == 0 then
    return -1 -- 不存在
end

local hasUser = redis.call('BF.EXISTS',key,userId)
if hasUser == 1 then
    return 1 -- 已存在 点过赞
end
-- 到这里表示存在但没点赞
redis.call('BF.ADD',key,userId) -- 添加
return 0