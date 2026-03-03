local key = KEYS[1]
local expireTime = tonumber(ARGV[1])

if redis.call('EXISTS', key) == 0 then

    -- 防止并发创建报错
    pcall(function()
        redis.call('BF.RESERVE', key, 0.01, 100000)
    end)

    local values = {}
    for i = 2, #ARGV do
        values[#values + 1] = ARGV[i]
    end

    if #values > 0 then
        redis.call('BF.MADD', key, unpack(values))
    end

    redis.call('EXPIRE', key, expireTime)

end

return 0