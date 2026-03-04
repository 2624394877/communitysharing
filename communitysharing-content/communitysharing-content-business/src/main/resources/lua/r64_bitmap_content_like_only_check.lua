local key = KEYS[1]
local contentId = ARGV[1]

if redis.call("EXISTS", key) == 0 then
  return -1 -- key does not exist
end

-- 0 = contentId is not in key, 1 = contentId is in key
return redis.call("R64.GETBIT", key, contentId)