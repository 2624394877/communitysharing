local bloomKey = KEYS[1]
local userId = ARGV[1]

if redis.call("EXISTS", bloomKey) == 0 then
  return -1 -- 不存在 没点赞
end

return redis.call("BF.EXISTS", bloomKey, userId) -- 1 表示存在（点赞）