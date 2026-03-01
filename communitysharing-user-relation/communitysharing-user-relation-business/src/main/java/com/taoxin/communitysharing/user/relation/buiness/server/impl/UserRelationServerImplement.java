package com.taoxin.communitysharing.user.relation.buiness.server.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import com.taoxin.communitysharing.user.relation.buiness.constant.MQConstant;
import com.taoxin.communitysharing.user.relation.buiness.constant.RedisKeyConstant;
import com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject.FansDo;
import com.taoxin.communitysharing.user.relation.buiness.domain.databaseObject.FollowingDo;
import com.taoxin.communitysharing.user.relation.buiness.domain.mapper.FansDoMapper;
import com.taoxin.communitysharing.user.relation.buiness.domain.mapper.FollowingDoMapper;
import com.taoxin.communitysharing.user.relation.buiness.enums.LuaResultEnum;
import com.taoxin.communitysharing.user.relation.buiness.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.user.relation.buiness.model.dto.FollowUserMqDTO;
import com.taoxin.communitysharing.user.relation.buiness.model.dto.UnfollowUserMqDTO;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.FindFansListReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.FollowingUserReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.FollowingUsersListReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.req.UnfollowUserReqVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.res.FindFansUsersListRseVo;
import com.taoxin.communitysharing.user.relation.buiness.model.vo.res.FollowingUsersListResVo;
import com.taoxin.communitysharing.user.relation.buiness.rpc.UserFeignApiService;
import com.taoxin.communitysharing.user.relation.buiness.server.UserRelationServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class UserRelationServerImplement implements UserRelationServer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private FollowingDoMapper followingDoMapper;
    @Resource
    private FansDoMapper fansDoMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserFeignApiService userFeignApiService;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Override
    public Response<?> followUser(FollowingUserReqVo followingUserReqVo) {
        Long userid = LoginUserContextHolder.getUserId(); // 获取用户id
        if (Objects.isNull(userid)) throw new BusinessException(ResponseStatusEnum.SYSTEMP_ERROR.getErrorCode(),"关注失败，请重试");
        Long followingUserId = followingUserReqVo.getFollowingUserId(); // 获取被关注用户id
        // 验证用户id
        if (Objects.equals(userid, followingUserId)) {
            throw new BusinessException(ResponseStatusEnum.CANT_FOLLOWING_YOURSELF);
        }
        // 验证被关注用户id是否存在
        FindUserByIdResDTO findUserByIdResDTO = userFeignApiService.getUserInfoById(followingUserId);
        if (Objects.isNull(findUserByIdResDTO)) {
            throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
        }
        // 构建redis key
        String followingUserIdKey = RedisKeyConstant.getUserFollowRelationKey(userid);
        // 构建lua脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        redisScript.setResultType(Long.class);
        // 构建时间戳
        LocalDateTime now = LocalDateTime.now();
        Long timestamp = DateUtil.LocalTimestampToDate(now);
        // 执行lua脚本
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(followingUserIdKey), followingUserId, timestamp);
        // 调用判断类型
        ValidatedLuaScriptResult(result);
        // 判断主键
        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) { //
            List<FollowingDo> followingDoList = followingDoMapper.selectByUserId(userid);
            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机生成过期时间
            log.info("followingDoList: {}", followingDoList);
            if (CollUtil.isEmpty(followingDoList)) { // 记录为空
                // 添加关注 并更新redis缓存,设置过期时间
                DefaultRedisScript<Long> addRedisScript = new DefaultRedisScript<>();
                addRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_follow_and_expire_time.lua")));
                addRedisScript.setResultType(Long.class); // 设置返回结果类型
                // 类型判断 通过粉丝数量判断是否为大用户，大用户的过期时间短，小用户的过期时间长

                // 将被关注用户id写入redis ZSET集合中
                redisTemplate.execute(addRedisScript, Collections.singletonList(followingUserIdKey), followingUserId, timestamp, expireTime);
            } else { // 记录不为空
                // 更新redis,设置过期时间
                // 构建 Lua 参数
                Object[] luaArgs = buildLuaArgs(followingDoList, expireTime);

                DefaultRedisScript<Long> updateRedisScript = new DefaultRedisScript<>();
                updateRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_follow_batch_and_expire_time.lua")));
                updateRedisScript.setResultType(Long.class);
                // 将被关注用户id写入redis ZSET集合中
                redisTemplate.execute(updateRedisScript, Collections.singletonList(followingUserIdKey), luaArgs);

                result = redisTemplate.execute(redisScript, Collections.singletonList(followingUserIdKey), followingUserId, timestamp);
                ValidatedLuaScriptResult(result);
            }
        }
        // 发送MQ消息，让订阅服务器关系数据库（异步）
        // 构建MQ消息
        FollowUserMqDTO followingUserMqDTO = FollowUserMqDTO.builder()
                .userId(userid)
                .followingUserId(followingUserId)
                .createTime(now)
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(followingUserMqDTO))
                .build();
        String destination = MQConstant.FOLLOW_UNFOLLOW_TOPIC+":"+MQConstant.FOLLOW_TAG;
        String hashKey = String.valueOf(userid); // 关注发起的用户id
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {

                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("follow消息队列(异步)发送成功: {}", sendResult);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("follow消息队列(异步)发送失败: {}", throwable.getMessage());
                    }
                }
        ); // 发送消息
        return Response.success("关注成功");
    }

    @Override
    public Response<?> UnfollowUser(UnfollowUserReqVo unfollowUserReqVo) {
        Long userid = LoginUserContextHolder.getUserId();
        Long unfollowingUserId = unfollowUserReqVo.getUnfollowUserId();
        if (Objects.isNull(userid)) throw new BusinessException(ResponseStatusEnum.SYSTEMP_ERROR.getErrorCode(),"取消关注失败，请重试");
        if (Objects.equals(userid, unfollowingUserId)) throw new BusinessException(ResponseStatusEnum.CANT_UN_FOLLOWING_YOURSELF);
        FindUserByIdResDTO findUserByIdResDTO = userFeignApiService.getUserInfoById(unfollowingUserId);
        if (Objects.isNull(findUserByIdResDTO)) throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
        String followingUserIdKey = RedisKeyConstant.getUserFollowRelationKey(userid);
        // 使用lua脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/unfollow_check_and_remove.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(followingUserIdKey), unfollowingUserId);
        // 检查是否被关注
        if (Objects.equals(result, LuaResultEnum.ZSCORE_NOT_FOLLOWED.getCode()))
            throw new BusinessException(ResponseStatusEnum.UN_FOLLOWED_USER);
        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            // redis 缓存不存在，进入到数据库查询
            List<FollowingDo> followingDoList = followingDoMapper.selectByUserId(userid);
            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机生成过期时间
            if (CollUtil.isEmpty(followingDoList)) {
                // 缓存不存在，数据库不存在， 抛出异常
                throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
            }else {
                // 数据库存在， 将所有的关注用户id写入redis ZSET集合中
                Object[] luaArgs = buildLuaArgs(followingDoList, expireTime);
                DefaultRedisScript<Long> updateRedisScript = new DefaultRedisScript<>();
                updateRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_follow_batch_and_expire_time.lua")));
                updateRedisScript.setResultType(Long.class);
                redisTemplate.execute(updateRedisScript, Collections.singletonList(followingUserIdKey), luaArgs);
                // 检查是否被关注，并删除
                result = redisTemplate.execute(redisScript, Collections.singletonList(followingUserIdKey), unfollowingUserId);
                if (Objects.equals(result, LuaResultEnum.ZSCORE_NOT_FOLLOWED.getCode())) {
                    throw new BusinessException(ResponseStatusEnum.UN_FOLLOWED_USER);
                }
            }
        }
        // 发送MQ消息，让订阅服务器关系数据库（异步）
        UnfollowUserMqDTO unfollowUserMqDTO = UnfollowUserMqDTO.builder()
                .userId(userid)
                .unfollowUserId(unfollowingUserId)
                .createTime(LocalDateTime.now())
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(unfollowUserMqDTO))
                .build();
        String destination = MQConstant.FOLLOW_UNFOLLOW_TOPIC+":"+MQConstant.UN_FOLLOW_TAG;
        log.info("==> 开始发送取关操作 MQ:{}",unfollowUserMqDTO);
        String hashKey = String.valueOf(userid); // 取关发起的用户id
        rocketMQTemplate.asyncSendOrderly(destination, message,hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("取消关注消息队列(异步)发送成功: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("取消关注消息队列(异步)发送失败: {}", throwable.getMessage());
            }
        });
        return Response.success("取消关注成功");
    }

    @Override
    public PageResponse<FollowingUsersListResVo> getFollowingList(FollowingUsersListReqVo followingUsersListReqVo) {
        Long userid = LoginUserContextHolder.getUserId();
        Long id = followingUsersListReqVo.getUserId(); // 需要查询的用户id
        Integer pageNumber = followingUsersListReqVo.getPageNumber();

        String followingUserIdKey = RedisKeyConstant.getUserFollowRelationKey(id);
        Long total = redisTemplate.opsForZSet().size(followingUserIdKey);
        List<FollowingUsersListResVo> followingUsersListResVo = null;
        // 缓存存在
        // 每页展示 10 条数据
        long limit = 10;
        if (Objects.nonNull(total) && total > 0) {
            // 计算总页数
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 判断：如果页码大于总页数，则返回错误信息
            if (pageNumber > totalPage) throw new BusinessException(ResponseStatusEnum.PAGE_NUMBER_ERROR);

            // 从redis中查询Zset分页数据
            // 每页 10 个元素，计算偏移量
            long offset = (pageNumber - 1) * limit;

            // 获取Zset分页数据
            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            // 这里使用了 Double.POSITIVE_INFINITY 和 Double.NEGATIVE_INFINITY 作为分数范围
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(followingUserIdKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);
            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                List<Long> followingUserIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                FindUsersByIdResponseDTO usersInfo = userFeignApiService.getUsersInfoByIds(followingUserIds);

                if (Objects.nonNull(usersInfo)) {
                    followingUsersListResVo = usersInfo.getUsersInfo().stream()
                        .map(userInfo -> FollowingUsersListResVo.builder()
                             .userId(userInfo.getId())
                             .communitysharingId(userInfo.getCommunitysharingId())
                             .nickname(userInfo.getNickname())
                             .avatar(userInfo.getAvatar())
                             .introduction(userInfo.getIntroduction())
                             .build())
                        .toList();
                }
            }
        }else {
            // TODO: 若 Redis 中没有数据，则从数据库查询
            // 先查询记录总量
            long count = followingDoMapper.selectCountByUserId(id);
            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(count, limit);
            // 请求的页码超出了总页数
            if (pageNumber > totalPage) throw new BusinessException(ResponseStatusEnum.PAGE_NUMBER_ERROR);
            // 偏移量
            long offset = PageResponse.getOffset(pageNumber, limit);
            // 分页查询
            List<FollowingDo> followingDOS = followingDoMapper.selectPageListByUserId(id, offset, limit);
            // 赋值真实的记录总数
            total = count;
            // 若记录不为空
            if (CollUtil.isNotEmpty(followingDOS)) {
                // 提取所有关注用户 ID 到集合中
                List<Long> userIds = followingDOS.stream().map(FollowingDo::getFollowingUserId).toList();

                // RPC: 调用用户服务，并将 DTO 转换为 VO
                followingUsersListResVo = rpcUserServiceAndDTO2VO(userIds, followingUsersListResVo);

                // TODO: 异步将关注列表全量同步到 Redis
                threadPoolTaskExecutor.submit(() -> syncFollowingList2Redis(id));
            }
        }
        return PageResponse.success(followingUsersListResVo, pageNumber, total);
    }

    @Override
    public PageResponse<FindFansUsersListRseVo> findFansList(FindFansListReqVo findFansListReqVO) {
        // 想要查询的用户 ID
        Long userId = findFansListReqVO.getUserId();
        // 页码
        Integer pageNo = findFansListReqVO.getPageNumber();

        // 先从 Redis 中查询
        String fansListRedisKey = RedisKeyConstant.getUserFansRelationKey(userId);

        // 查询目标用户粉丝列表 ZSet 的总大小
        long total = redisTemplate.opsForZSet().zCard(fansListRedisKey);

        // 返参
        List<FindFansUsersListRseVo> fansUsersListRseVos = null;

        // 每页展示 10 条数据
        long limit = 10;

        if (total > 0) { // 缓存中有数据
            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);
            // 请求的页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);
            // 准备从 Redis 中查询 ZSet 分页数据
            // 每页 10 个元素，计算偏移量
            long offset = PageResponse.getOffset(pageNo, limit);
            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(fansListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);
            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                // 提取所有用户 ID 到集合中
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC: 批量查询用户信息
                FindUsersByIdResponseDTO userinfo = userFeignApiService.getUsersInfoByIds(userIds);
                fansUsersListRseVos = userinfo.getUsersInfo().stream()
                        .map(userInfo -> FindFansUsersListRseVo.builder()
                                .userId(userInfo.getId())
                                .communitysharingId(userInfo.getCommunitysharingId())
                                .nickname(userInfo.getNickname())
                                .avatar(userInfo.getAvatar())
                                .introduction(userInfo.getIntroduction())
                                .build())
                        .toList();
                log.info("=====>fansUsersListRseVos:{}",userinfo);
            }
        } else { // 若 Redis 缓存中无数据，则查询数据库
//            log.info("[UserRelationServerImplement] 查询用户ID为{}的粉丝列表", userId);
            // 先查询记录总量
            total = fansDoMapper.selectCountByUserId(userId);

            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数（只允许查询前 500 页）
            if (pageNo > 500 || pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 分页查询
            List<FansDo> fansDOS = fansDoMapper.selectPageListByUserId(userId, offset, limit);
            // 若记录不为空
            if (CollUtil.isNotEmpty(fansDOS)) {
                // 提取所有粉丝用户 ID 到集合中
                List<Long> userIds = fansDOS.stream().map(FansDo::getFansUserId).toList();

                // RPC: 调用用户服务、计数服务，并将 DTO 转换为 VO
                fansUsersListRseVos = rpcUserServiceAndCountServiceAndDTO2VO(userIds, fansUsersListRseVos);
                // 异步将粉丝列表同步到 Redis（最多5000条）
                threadPoolTaskExecutor.submit(() -> syncFansList2Redis(userId));
            }
        }
        return PageResponse.success(fansUsersListRseVos, pageNo, total);
    }

    /**
     * 同步粉丝列表到 Redis
     * @param userId
     */
    private void syncFansList2Redis(Long userId) {
        List<FansDo> fansDOS = fansDoMapper.select5000FansByUserId(userId);
        if (CollUtil.isNotEmpty(fansDOS)) {
            String fansListRedisKey = RedisKeyConstant.getUserFansRelationKey(userId);
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildFansZSetLuaArgs(fansDOS, expireSeconds);

            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_follow_batch_and_expire_time.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(fansListRedisKey), luaArgs);
        }
    }

    private Object[] buildFansZSetLuaArgs(List<FansDo> fansDOS, long expireSeconds) {
        int argsLength = fansDOS.size() * 2 + 1; // 每个粉丝关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];
        int i = 0;
        for (FansDo fansDo : fansDOS) {
            luaArgs[i] = DateUtil.LocalTimestampToDate(fansDo.getCreateTime()); // 粉丝的关注时间作为 score
            luaArgs[i + 1] = fansDo.getFansUserId();          // 粉丝的用户 ID 作为 ZSet value
            i += 2;
        }
        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }


    /**
     * RPC: 调用用户服务、计数服务，并将 DTO 转换为 VO
     * @param userIds
     * @param fansUsersListRseVos
     * @return
     */
    private List<FindFansUsersListRseVo> rpcUserServiceAndCountServiceAndDTO2VO(List<Long> userIds, List<FindFansUsersListRseVo> fansUsersListRseVos) {
        FindUsersByIdResponseDTO findUsersByIdResponseDTO = userFeignApiService.getUsersInfoByIds(userIds);
        List<FindUsersByIdResDTO> findUsersByIdResDTOS = findUsersByIdResponseDTO.getUsersInfo();
        if (CollUtil.isNotEmpty(findUsersByIdResDTOS)) {
            fansUsersListRseVos = findUsersByIdResDTOS.stream()
                    .map(findUsersByIdResDTO -> FindFansUsersListRseVo.builder()
                            .userId(findUsersByIdResDTO.getId())
                            .communitysharingId(findUsersByIdResDTO.getCommunitysharingId())
                            .nickname(findUsersByIdResDTO.getNickname())
                            .avatar(findUsersByIdResDTO.getAvatar())
                            .introduction(findUsersByIdResDTO.getIntroduction())
                            .build())
                    .toList();
        }
        return fansUsersListRseVos;
    }

    private List<FollowingUsersListResVo> rpcUserServiceAndDTO2VO(List<Long> userIds, List<FollowingUsersListResVo> followingUsersListResVo) {
        FindUsersByIdResponseDTO findUsersByIdResponseDTO = userFeignApiService.getUsersInfoByIds(userIds);
        List<FindUsersByIdResDTO> findUsersByIdResDTOS = findUsersByIdResponseDTO.getUsersInfo();
        if (CollUtil.isNotEmpty(findUsersByIdResDTOS)) {
            followingUsersListResVo = findUsersByIdResDTOS.stream()
                    .map(findUsersByIdResDTO -> FollowingUsersListResVo.builder()
                            .userId(findUsersByIdResDTO.getId())
                            .communitysharingId(findUsersByIdResDTO.getCommunitysharingId())
                            .nickname(findUsersByIdResDTO.getNickname())
                            .avatar(findUsersByIdResDTO.getAvatar())
                            .introduction(findUsersByIdResDTO.getIntroduction())
                            .build())
                    .toList();
        }
        return followingUsersListResVo;
    }

    /**
     * 同步关注列表到 Redis
     * @param userId
     */
    private void syncFollowingList2Redis(Long userId) {
        List<FollowingDo> followingDoList = followingDoMapper.selectAllByUserId(userId);
        log.info("=========>followingDoList:{}",followingDoList);
        if (CollUtil.isNotEmpty(followingDoList)) {
            // 用户关注列表 Redis Key
            String followingListRedisKey = RedisKeyConstant.getUserFollowRelationKey(userId);
            long expireTime = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildLuaArgs(followingDoList, expireTime);

            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/add_follow_batch_and_expire_time.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(followingListRedisKey),luaArgs);
        }
    }


    /**
     * 构建Lua参数
     * @param followingDoList 关注列表
     * @param expireTime 过期时间
     * @return 参数数组
     */
    private Object[] buildLuaArgs(List<FollowingDo> followingDoList, long expireTime) {
        int size = followingDoList.size() *2 + 1; // 一个元素占两个位置，最后一个元素占一个位置
        Object[] luaArgs = new Object[size]; // 创建一个对象数组

        int index = 0;
        for (FollowingDo followingDo : followingDoList) {
            luaArgs[index] = DateUtil.LocalTimestampToDate(followingDo.getCreateTime());
            luaArgs[index+1] = followingDo.getFollowingUserId();
            index += 2; // 跳过一个位置
        }
        luaArgs[size-1] = expireTime; // 最后一个参数,为过期时间
        return luaArgs;
    }

    /**
     * 验证Lua脚本结果
     * @param result Lua脚本结果
     */
    private void ValidatedLuaScriptResult(Long result) {
        LuaResultEnum luaResultEnum = LuaResultEnum.getByCode(result);
        if (Objects.isNull(luaResultEnum)) {
            throw new BusinessException(ResponseStatusEnum.FOLLOWING_USER_ERROR);
        }
        switch (luaResultEnum) {
            case ZCARD_OVER_MAX_SIZE -> throw new BusinessException(ResponseStatusEnum.FOLLOW_NUMBER_OVER_MAX_SIZE);
            case ZSCORE_FOLLOWED -> throw new BusinessException(ResponseStatusEnum.FOLLOWED_USER);
        }
    }
}
