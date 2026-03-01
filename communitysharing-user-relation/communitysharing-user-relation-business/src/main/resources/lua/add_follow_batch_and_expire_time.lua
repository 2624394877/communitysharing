local key = KEYS[1]

-- 批量添加数据的参数
local zaddArgs = {}

-- 遍历 ARGV 参数，将分数和值按顺序插入到 zaddArgs 变量中
for i = 1, #ARGV - 1 , 2 do
  table.insert(zaddArgs, ARGV[i])
  table.insert(zaddArgs, ARGV[i + 1])
end

-- 批量添加数据
redis.call('ZADD', key, unpack(zaddArgs))

-- 设置过期时间
redis.call('EXPIRE', key, ARGV[#ARGV])

return 0 -- 返回成功