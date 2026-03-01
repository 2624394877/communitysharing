local key = KEYS[1]
local contentId = ARGV[1]

local exist = redis.call('EXISTS', key) -- 检查布隆过滤器中是否存在该内容ID
if exist == 0 then
    return -1 -- 不存在，返回-1
end

local cellected = redis.call('BF.EXISTS', key, contentId) -- 检查布隆过滤器中是否已经收集过该内容ID
if cellected == 1 then
    return 1 -- 已经收集过，返回1，表示已经收藏过了
end

-- 不存在走这里，则表示没有收藏过，更新布隆过滤器
redis.call('BF.ADD', key, contentId) -- 将内容ID添加到布隆过滤器中
return 0 -- 返回0，表示没有收藏过，现在已经收藏了