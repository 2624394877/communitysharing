package com.taoxin.communitysharing.comment.business.service.implement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.KV.dto.request.DeleteCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.request.FindCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.comment.business.constant.CommentContentKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.CountConentRedisKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentLikeDoMapper;
import com.taoxin.communitysharing.comment.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.comment.business.enums.*;
import com.taoxin.communitysharing.comment.business.model.dto.LikeUnLikeCommentDTO;
import com.taoxin.communitysharing.comment.business.model.dto.PublishCommentMqDTO;
import com.taoxin.communitysharing.comment.business.model.vo.req.*;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindCommentItemRspVo;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindSecondCommentItemRspVo;
import com.taoxin.communitysharing.comment.business.retry.SendMQRetryHelper;
import com.taoxin.communitysharing.comment.business.rpc.DistributedIdGeneratorRpcService;
import com.taoxin.communitysharing.comment.business.rpc.KVFeignApiService;
import com.taoxin.communitysharing.comment.business.rpc.UserFeignApiService;
import com.taoxin.communitysharing.comment.business.service.CommentServer;
import com.taoxin.communitysharing.common.constant.DateConstants;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServerImplement implements CommentServer {
    @Resource
    private SendMQRetryHelper sendMQRetryHelper;
    @Resource
    private DistributedIdGeneratorRpcService commentIdGenerator;
    @Resource
    private CommentDoMapper commentDoMapper;
    @Resource
    private ContentCountDoMapper contentCountDoMapper;
    @Resource
    private KVFeignApiService kvFeignApiService;
    @Resource
    private UserFeignApiService userFeignApiService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    @Resource
    private CommentLikeDoMapper commentLikeDoMapper;
    @Resource
    private TransactionTemplate transactionTemplate;

    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 初始化缓存大小
            .maximumSize(10000) // 最大缓存数量
            .expireAfterWrite(1, TimeUnit.HOURS) // 缓存过期时间
            .build(); // 创建缓存对象
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public Response<?> publishComtent(CommentPublishReqVo commentPublishReqVo) {
        String content = commentPublishReqVo.getContent();
        String imageUrl = commentPublishReqVo.getImageUrl();
        // 内容和图片不能都为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl), "评论不能为空");

        // todo 评论逻辑
        Long creatorId = LoginUserContextHolder.getUserId();
        String commentId = commentIdGenerator.getCommentId();
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .contentId(commentPublishReqVo.getContentId())
                .commentId(Long.valueOf(commentId))
                .content(content)
                .imageUrl(imageUrl)
                .replayCommentId(commentPublishReqVo.getReplayCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        // 发送MQ
        sendMQRetryHelper.sendMQ(MQConstant.TOPIC_PUBLISH_COMMENT, JsonUtil.toJsonString(publishCommentMqDTO),"评论服务");
        // 删除Caffeine
        log.info("删除Caffeine id: {}",Long.valueOf(commentId));
        LOCAL_CACHE.invalidate(commentPublishReqVo.getReplayCommentId());
        return Response.success();
    }

    @Override
    public PageResponse<FindCommentItemRspVo> findCommentPageList(FindCommentPageListReqVo findCommentPageListReqVo) {
        // 内容ID
        Long contentId = findCommentPageListReqVo.getContentId();
        // 当前页码
        Integer pageNo = findCommentPageListReqVo.getPageNo();
        // 设置每一页需要的数据
        long pageSize = 10;

        // todo 查缓存
        String countContentRedisKey = CountConentRedisKeyConstant.getCountContentKeyPrefix(contentId);
        // 获取评论总数
        Number commentCount = (Number) (redisTemplate.opsForHash().get(countContentRedisKey, CountConentRedisKeyConstant.COMMENT_TOTAL));

        long totalCount = Objects.isNull(commentCount) ? 0L : commentCount.longValue();
        // todo 数据库查询
        // 若缓存不存在，则查询数据库
        if (Objects.isNull(commentCount)) {
            Long count = contentCountDoMapper.selectCountCommentByContentId(contentId);

            if (Objects.isNull(count)) {
                throw new BusinessException(ResponseStatusEnum.COMMENT_NOT_EXIST);
            }
            totalCount = count;

            taskExecutor.submit(() -> {
                // todo 总数存入redis
                asyncSetCommentCount(countContentRedisKey, count);
            });
        }
        // 评论总数为0 直接返回
        if (Objects.equals(totalCount, 0L)) return PageResponse.success(null, pageNo, 0L, pageSize);
        // 分页返参
        List<FindCommentItemRspVo> findCommentItemRspVoList = null;
        findCommentItemRspVoList = Lists.newArrayList();
        // 计算offset
        long offset = PageResponse.getOffset(pageNo, pageSize);
        // 评论分页缓存使用 ZSET + STRING 实现
        // 构建评论 ZSET Key
        String commentZsetKey = CommentContentKeyConstant.getCommentListId(contentId);
        boolean isExist = redisTemplate.hasKey(commentZsetKey);

        // 不存在 异步将热点评论同步到 redis 中（最多同步 500 条）
        if (!isExist) {
            taskExecutor.submit(() -> {
                syncHotComment2Redis(commentZsetKey, contentId);
            });
        }

        if (isExist && totalCount < 500) {
            // todo 集合存在且评论总数小于500，就到redis中拿到一级评论列表
            // 使用 ZRevRange 获取某篇内容下，按热度降序排序的一级评论 ID
            Set<Object> commentIdSet = redisTemplate.opsForZSet().reverseRangeByScore(commentZsetKey, -Double.MAX_VALUE, Double.MAX_VALUE, offset, pageSize);
            if (CollUtil.isNotEmpty(commentIdSet)) {
                // Set 转 List
                List<Object> commentIdList = Lists.newArrayList(commentIdSet);

                // todo 查本地缓存
                List<Long> localCacheCommentIdsExist = Lists.newArrayList(); // 本地缓存中不存在的评论 ID
                List<Long> cacheKey = commentIdList.stream()
                        .map(commentId -> Long.valueOf(String.valueOf(commentId)))
                        .toList();
                // 批量查询本地缓存
                Map<Long, String> commentIdAndDetailJsonMap = LOCAL_CACHE.getAll(cacheKey, commentkeys->{
                    Map<Long, String> missingCommentIds = Maps.newHashMap();
                    // 对于本地缓存中缺失的 key，返回空字符串
                    commentkeys.forEach(commentId -> {
                        // 记录缓存中不存在的评论 ID
                        localCacheCommentIdsExist.add(commentId);
                        // 不存在的评论详情, 对其 Value 值设置为空字符串
                        missingCommentIds.put(commentId, "");
                    });
                    return missingCommentIds;
                });

                // 若 localCacheCommentIdsExist 的大小不等于 commentIdList 的大小，说明本地缓存中有数据
                if (!Objects.equals(CollUtil.size(localCacheCommentIdsExist), commentIdList.size())) {
                    // 将本地缓存中的评论详情 Json, 转换为实体类，添加到 VO 返参集合中
                    for (String commentDetailJson : commentIdAndDetailJsonMap.values()) {
                        if (StrUtil.isBlank(commentDetailJson)) continue;
                        findCommentItemRspVoList.add(JsonUtil.parseObject(commentDetailJson, FindCommentItemRspVo.class));
                    }
                }

                // 若 localCacheCommentIdsExist 大小等于 0，说明评论详情数据都在本地缓存中，直接响应返参
                if (Objects.equals(CollUtil.size(localCacheCommentIdsExist), 0)) {

                    // 修改计数数据
                    if (CollUtil.isNotEmpty(findCommentItemRspVoList)) {
                        setCommentCountData(findCommentItemRspVoList, localCacheCommentIdsExist);
                    }

                    return PageResponse.success(findCommentItemRspVoList, pageNo, totalCount, pageSize);
                }

                // 构建 MGET 批量查询评论详情的 Key 集合
                List<String> commentDetailKeys = localCacheCommentIdsExist.stream()
                        .map(CommentContentKeyConstant::getCommentDetail)
                        .toList();
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentDetailKeys);
                // 可能存在部分评论不在缓存中，已经过期被删除，这些评论 ID 需要提取出来，等会查数据库
                List<Long> expiredCommentIds = Lists.newArrayList();
                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
//                    log.info("commentJson: {}", commentJson);
                    if (Objects.nonNull(commentJson)) {
                        // 缓存中存在的评论 Json，直接转换为 VO 添加到返参集合中
                        FindCommentItemRspVo findCommentItemRspVo = JsonUtil.parseObject(commentJson, FindCommentItemRspVo.class);
                        findCommentItemRspVoList.add(findCommentItemRspVo);

                    } else {
                        // 缓存中不存在的评论 ID，添加到待查询的集合中
                        expiredCommentIds.add(Long.valueOf(localCacheCommentIdsExist.get(i).toString()));
                    }
                }

                // 这里都是从redis中拿的数据，需要更新计数
                if (CollUtil.isNotEmpty(findCommentItemRspVoList)) {
                    setCommentCountData(findCommentItemRspVoList, expiredCommentIds);
                }

                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    // 对于不存在的一级评论，需要批量从数据库中查询，并添加到 findCommentItemRspVoList 中
                    List<CommentDo> commentDoList = commentDoMapper.selectByCommentIds(expiredCommentIds);
                    getCommentDataAndSync2Redis(commentDoList, contentId, findCommentItemRspVoList);
                }
            }
            // todo 根据拿到的id表去redis中拿数据，并返回
            if (CollUtil.isNotEmpty(findCommentItemRspVoList)) {
                // 按热度值进行降序排列
                findCommentItemRspVoList = findCommentItemRspVoList.stream()
                        //Comparator.comparing() 比较器： 默认升序
                        .sorted(Comparator.comparing(FindCommentItemRspVo::getHeat).reversed()
                                .thenComparing(Comparator.comparing(FindCommentItemRspVo::getUpdateTime).reversed()))
                        .toList();
                getCommentDataAndSync2Cache(findCommentItemRspVoList); // 本地缓存一级评论
                return PageResponse.success(findCommentItemRspVoList, pageNo, totalCount, pageSize);
            }
        }

        log.info("一级评论缓存不存在，查询数据库");
        // 缓存不存在，则从数据库中查询
        // 查询一级评论
        List<CommentDo> commentDoList = commentDoMapper.selectPageList(contentId, offset, pageSize);
//        log.info("一级评论：{}", commentDoList);
        getCommentDataAndSync2Redis(commentDoList, contentId, findCommentItemRspVoList);
        getCommentDataAndSync2Cache(findCommentItemRspVoList); // 本地缓存一级评论
        return PageResponse.success(findCommentItemRspVoList, pageNo, totalCount, pageSize);
    }

    @Override
    public PageResponse<FindSecondCommentItemRspVo> findChildCommentPageList(SecondCommentPageListReqVo secondCommentPageListReqVo) {
        Long level1CommentId = secondCommentPageListReqVo.getLeve1CommentId();
        Integer pageNo = secondCommentPageListReqVo.getPageNo();
        long pageSize = 10;

        // todo 缓存查询
        String commentZsetKey = CountConentRedisKeyConstant.getCountCommentKeyPrefix(level1CommentId);
        Number score = (Number) (redisTemplate.opsForHash().get(commentZsetKey, CountConentRedisKeyConstant.CHILD_COMMENT_TOTAL));
        long totalCount = Objects.isNull(score) ? 0 : score.longValue();

        if (Objects.isNull(score)) {
            Long count = commentDoMapper.selectSecondCommentCountByContentId(level1CommentId);

            if (Objects.isNull(count)) {
                throw new BusinessException(ResponseStatusEnum.COMMENT_NOT_EXIST);
            }
            totalCount = count;
            taskExecutor.submit(()->{
                asyncSetChildCommentTotal(commentZsetKey, count);
            });
        }

        // 评论总数为0 直接返回
        if (Objects.equals(totalCount, 0L)) return PageResponse.success(null, pageNo, 0L, pageSize);

        // todo 数据库查询
        if (totalCount == 0) return PageResponse.success(null, pageNo, 0, pageSize);

        List<FindSecondCommentItemRspVo> findSecondCommentItemRspVoList = Lists.newArrayList();

        long offset = PageResponse.getOffset(pageNo, pageSize) + 1; //数据库起始位置

        String commentChildZSetKey = CommentContentKeyConstant.getCommentChildListId(level1CommentId);
        boolean isExist = redisTemplate.hasKey(commentChildZSetKey);
        if (!isExist) {
            taskExecutor.submit(()->{
                asyncSetCommentChildList(commentChildZSetKey, level1CommentId);
            });
        }

        if (isExist && totalCount <= 70) {
            // 使用 ZRevRange 获取某篇内容下，按热度降序排序的一级评论 ID
            Set<Object> commentIdSet = redisTemplate.opsForZSet().reverseRangeByScore(commentChildZSetKey, -Double.MAX_VALUE, Double.MAX_VALUE, offset, pageSize);

            if (CollUtil.isNotEmpty(commentIdSet)) {
                // Set 转 List
                List<Object> commentIdList = Lists.newArrayList(commentIdSet);

                // todo 查本地缓存
                List<Long> localCacheCommentIdsExist = Lists.newArrayList(); // 本地缓存中不存在的评论 ID
                List<Long> cacheKey = commentIdList.stream()
                        .map(commentId -> Long.valueOf(String.valueOf(commentId)))
                        .toList();
                // 批量查询本地缓存
                Map<Long, String> commentIdAndDetailJsonMap = LOCAL_CACHE.getAll(cacheKey, commentkeys->{
                    Map<Long, String> missingCommentIds = Maps.newHashMap();
                    // 对于本地缓存中缺失的 key，返回空字符串
                    commentkeys.forEach(commentId -> {
                        // 记录缓存中不存在的评论 ID
                        localCacheCommentIdsExist.add(commentId);
                        // 不存在的评论详情, 对其 Value 值设置为空字符串
                        missingCommentIds.put(commentId, "");
                    });
                    return missingCommentIds;
                });

                // 若 localCacheCommentIdsExist 的大小不等于 commentIdList 的大小，说明本地缓存中有数据
                if (!Objects.equals(CollUtil.size(localCacheCommentIdsExist), commentIdList.size())) {
                    // 将本地缓存中的评论详情 Json, 转换为实体类，添加到 VO 返参集合中
                    for (String commentDetailJson : commentIdAndDetailJsonMap.values()) {
                        findSecondCommentItemRspVoList.add(JsonUtil.parseObject(commentDetailJson, FindSecondCommentItemRspVo.class));
                    }
                }

                // 若 localCacheCommentIdsExist 大小等于 0，说明评论详情数据都在本地缓存中，直接响应返参
                if (Objects.equals(CollUtil.size(localCacheCommentIdsExist), 0)) {

                    // 修改计数数据
                    if (CollUtil.isNotEmpty(findSecondCommentItemRspVoList)) {
                        log.info("修改计数数据: {}",findSecondCommentItemRspVoList);
                        setSecondCommentCountData(findSecondCommentItemRspVoList, localCacheCommentIdsExist);
                    }

                    return PageResponse.success(findSecondCommentItemRspVoList, pageNo, totalCount, pageSize);
                }

                // 构建 MGET 批量查询评论详情的 Key 集合
                List<String> commentDetailKeys = localCacheCommentIdsExist.stream()
                        .map(CommentContentKeyConstant::getCommentChildDetail)
                        .toList();
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentDetailKeys);

                // 可能存在部分评论不在缓存中，已经过期被删除，这些评论 ID 需要提取出来，等会查数据库
                List<Long> expiredCommentIds = Lists.newArrayList();
                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
                    if (Objects.nonNull(commentJson)) {
                        // 缓存中存在的评论 Json，直接转换为 VO 添加到返参集合中
                        FindSecondCommentItemRspVo findCommentItemRspVo = JsonUtil.parseObject(commentJson, FindSecondCommentItemRspVo.class);
                        findSecondCommentItemRspVoList.add(findCommentItemRspVo);
                    } else {
                        // 缓存中不存在的评论 ID，添加到待查询的集合中
                        expiredCommentIds.add(Long.valueOf(commentIdList.get(i).toString()));
                    }
                }
                // 这里都是从redis中拿的数据，需要更新计数
                if (CollUtil.isNotEmpty(findSecondCommentItemRspVoList)) {
                    setSecondCommentCountData(findSecondCommentItemRspVoList, expiredCommentIds);
                }

                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    // 对于不存在的评论，需要批量从数据库中查询，并添加到 findCommentItemRspVoList 中
                    List<CommentDo> commentDoList = commentDoMapper.selectByCommentIds(expiredCommentIds);
                    getSecondCommentDataAndSync2Redis(commentDoList, findSecondCommentItemRspVoList);
                }

                if (CollUtil.isNotEmpty(findSecondCommentItemRspVoList)) {
                    // 按热度值进行降序排列
                    findSecondCommentItemRspVoList = findSecondCommentItemRspVoList.stream()
                            //Comparator.comparing() 比较器： 默认升序
                            .sorted(Comparator.comparing(FindSecondCommentItemRspVo::getCreateTime).reversed()
                                    .thenComparing(Comparator.comparing(FindSecondCommentItemRspVo::getLikeTotal)).reversed())
                            .toList();
                    // 本地缓存评论
                    getSecondCommentDataAndSync2Cache(findSecondCommentItemRspVoList);
                    return PageResponse.success(findSecondCommentItemRspVoList, pageNo, totalCount, pageSize);
                }
            }
        }

        // 批量查询
        List<CommentDo> commentDoList = commentDoMapper.selectSecondCommentByLeve1CommentId(level1CommentId, offset, pageSize);

        getSecondCommentDataAndSync2Redis(commentDoList, findSecondCommentItemRspVoList);

        return PageResponse.success(findSecondCommentItemRspVoList, pageNo, totalCount, pageSize);
    }

    @Override
    public Response<?> LikeComment(LikeCommentReqVo likeCommentReqVo) {
        Long commentId = likeCommentReqVo.getCommentId();
        // 判断点赞的评论是否存在
        checkedIsExistComment(commentId);
        // 判断评论是否已经点赞
        String likeCommentBloomKey = RedisKeyConstant.getBloomCommentLikesKey(commentId);
        Long userId = LoginUserContextHolder.getUserId();
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom_checked_like_comment.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Lists.newArrayList(likeCommentBloomKey), userId);
        CommentLikeLuaResultEnum commentLikeLuaResultEnum = CommentLikeLuaResultEnum.getByCode(result);
        if (Objects.isNull(commentLikeLuaResultEnum)) throw new BusinessException(ResponseStatusEnum.PARAMS_ERROR);
        switch (commentLikeLuaResultEnum) {
            case NOT_EXIST -> {
                int liked = commentLikeDoMapper.selectByCommentIdAndUserId(commentId, userId);
                long expireTime = 60 * 60 * 12 + RandomUtil.randomInt(60 * 60);
                if (liked > 0) { // 已点赞
                    // 更新过滤器
                    asyncSetbloomCommentLikes(likeCommentBloomKey, commentId,expireTime);
                    throw new BusinessException(ResponseStatusEnum.ALREADY_LIKED);
                }

                // 数据库没有，将commentId加入到过滤器中
                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom_add_comment_like_and_expire.lua")));
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Lists.newArrayList(likeCommentBloomKey), userId, expireTime);
            }
            case LIKED -> {
                // 验证
                int liked = commentLikeDoMapper.selectByCommentIdAndUserId(commentId, userId);
                if (liked > 0) throw new BusinessException(ResponseStatusEnum.ALREADY_LIKED);
            }
        }
        // 点赞后发送消息落库
        LikeUnLikeCommentDTO likeUnLikeCommentDTO = LikeUnLikeCommentDTO.builder()
                .commentId(commentId)
                .userId(userId)
                .type(LikeUnLikeTypeEnum.LIKE.getCode())
                .createTime(LocalDateTime.now())
                .build();
        String topicTag = MQConstant.TOPIC_COMMENT_LIKE + ":" + MQConstant.TOPIC_COMMENT_LIKE_TAG;
        String hashkey = String.valueOf(commentId);// 以内容id作为hashkey，保证同一内容的点赞消息落到同一个队列中，保证顺序
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(likeUnLikeCommentDTO)).build();
        rocketMQTemplate.asyncSendOrderly(topicTag, message, hashkey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【评论点赞消息】 ====> {},消息体: {}", sendResult, JsonUtil.toJsonString(likeUnLikeCommentDTO));
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("========= 【评论点赞消息】 ====> {}", throwable);
            }
        });

        return Response.success("点赞成功");
    }

    @Override
    public Response<?> unlikeComment(UnlikeCommentReqVo unlikeCommentReqVo) {
        Long commentId = unlikeCommentReqVo.getCommentId();
        Long userId = LoginUserContextHolder.getUserId();

        checkedIsExistComment(commentId);

        String likeCommentBloomKey = RedisKeyConstant.getBloomCommentLikesKey(commentId);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom_comment_unlike_check.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Lists.newArrayList(likeCommentBloomKey), userId);
        CommentUnlikeLuaResultEnum likeUnLikeTypeEnum = CommentUnlikeLuaResultEnum.getByCode(result);
        if (Objects.isNull(likeUnLikeTypeEnum)) throw new BusinessException(ResponseStatusEnum.PARAMS_ERROR);
        switch (likeUnLikeTypeEnum) {
            case NOT_EXISTS -> {
                // 可能是过期了
                taskExecutor.execute(()->{
                    long expireTime = 60 * 60 * 12 + RandomUtil.randomInt(60 * 60);
                    asyncSetbloomCommentLikes(likeCommentBloomKey, commentId,expireTime);
                });
                int liked = commentLikeDoMapper.selectByCommentIdAndUserId(commentId, userId);
                log.info("========= 【取消点赞】 ====> {}", liked);
                if (liked <= 0) throw new BusinessException(ResponseStatusEnum.NOT_LIKED);
            }
            case UNLIKED -> throw new BusinessException(ResponseStatusEnum.NOT_LIKED);
            case LIKED -> {
                int liked = commentLikeDoMapper.selectByCommentIdAndUserId(commentId, userId);
                if (liked <= 0) throw new BusinessException(ResponseStatusEnum.NOT_LIKED);
            }
        }
        // 取消点赞后发送消息落库
        LikeUnLikeCommentDTO likeUnLikeCommentDTO = LikeUnLikeCommentDTO.builder()
                .commentId(commentId)
                .userId(userId)
                .type(LikeUnLikeTypeEnum.UNLIKE.getCode())
                .createTime(LocalDateTime.now())
                .build();
        String topicTag = MQConstant.TOPIC_COMMENT_LIKE + ":" + MQConstant.TOPIC_COMMENT_UNLIKE_TAG;
        String hashkey = String.valueOf(commentId);
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(likeUnLikeCommentDTO)).build();
        rocketMQTemplate.asyncSendOrderly(topicTag, message, hashkey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【取消评论点赞消息】 ====> {},消息体: {}", sendResult, JsonUtil.toJsonString(likeUnLikeCommentDTO));
            }

            @Override
            public void onException(Throwable throwable) {
                log.info("========= 【取消评论点赞消息】 ====> {}", throwable);
            }
        });
        return Response.success("取消点赞成功");
    }

    @Override
    public Response<?> deleteComment(DeleteCommentReqVo deleteCommentReqVo) {
        // 1. 校验评论是否存在
        Long commentId = deleteCommentReqVo.getCommentId();
        // 2. 校验是否有权限删除
        Long userId = LoginUserContextHolder.getUserId();

        CommentDo commentDo = commentDoMapper.selectByPrimaryKey(commentId);
        if (Objects.isNull(commentDo)) throw new BusinessException(ResponseStatusEnum.COMMENT_NOT_EXIST);
        Long creatorId = commentDo.getUserId();
        if (!creatorId.equals(userId)) throw new BusinessException(ResponseStatusEnum.COMMENT_CANT_OPERATE);
        // 3. 物理删除评论、评论内容
        transactionTemplate.execute(transactionStatus -> {
            try {
                commentDoMapper.deleteByPrimaryKey(commentId);
                return null;
            } catch (Exception e) {
                log.error("========= 【删除评论】 ====> {}", e);
                transactionStatus.setRollbackOnly();
                throw e;
            }
        });
        DeleteCommentContentReqDTO deleteCommentContentReqDTO = DeleteCommentContentReqDTO.builder()
                .contentId(commentDo.getContentId())
                .yearMonth(commentDo.getCreateTime().format(DateTimeFormatter.ofPattern(DateConstants.YEAR_MONTH_PATTERN)))
                .commentId(commentDo.getContentUuid())
                .build();
        boolean deleteResult = kvFeignApiService.deleteCommentContent(deleteCommentContentReqDTO);
        if (!deleteResult) throw new BusinessException(ResponseStatusEnum.DELETED_COMMENT_FAIL);
        // 4. 删除 Redis 缓存（ZSet 和 String）
        Integer level = commentDo.getLevel();
        Long contentId = commentDo.getContentId();
        Long parentCommentId = commentDo.getParentId();
        // 判断级别
        String ZSetKey = Objects.equals(level, CommentLevelEnum.FIRST_LEVEL.getCode()) ?
                CommentContentKeyConstant.getCommentListId(contentId) : CommentContentKeyConstant.getCommentChildListId(parentCommentId);
        String stringKey = Objects.equals(level, CommentLevelEnum.FIRST_LEVEL.getCode()) ?
                CommentContentKeyConstant.getCommentDetail(commentId) : CommentContentKeyConstant.getCommentChildDetail(commentId);
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.opsForZSet().remove(ZSetKey, commentId);

                operations.delete(stringKey);
                return null;
            }
        });
        // 5. 发布广播 MQ, 将本地缓存删除
        String topic = MQConstant.TOPIC_COMMENT_DELETE;
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(deleteCommentReqVo)).build();
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【删除评论本地】 ====> {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("========= 【删除评论本地】 ====> {}", throwable);
            }
        });

        // 6. 发送 MQ, 异步去更新计数、删除关联评论、热度值等
        Message<String> countMessage = MessageBuilder.withPayload(JsonUtil.toJsonString(commentDo)).build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_COMMENT_DELETE_COUNT, countMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("========= 【删除评论计数】 ====> {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("========= 【删除评论计数】 ====> {}", throwable);
            }
        });
        return Response.success();
    }

    private void asyncSetbloomCommentLikes(String likeCommentBloomKey, Long commentId, long expireTime) {
        taskExecutor.submit(() -> {
            try {
                List<Long> userIdList = commentLikeDoMapper.selectByCommentId(commentId);
                if (CollUtil.isNotEmpty(userIdList)) {
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/bloom_batch_add_comment_like_and_expire.lua")));
                    redisScript.setResultType(Long.class);
                    List<Object> args = Lists.newArrayList();
                    args.addAll(userIdList);
                    args.add(expireTime);
                    redisTemplate.execute(redisScript, Lists.newArrayList(likeCommentBloomKey), args);
                }
            } catch (Exception e) {
                log.error("========= 【点赞消息】 异步更新过滤器失败：{}", e);
            }
        });
    }

    private void checkedIsExistComment(Long commentId) {
        // 现在本地找
        String commentJson = LOCAL_CACHE.getIfPresent(commentId);
        if (Objects.isNull(commentJson)) {
            // 再到redis找
            String CommentDetailKey = CommentContentKeyConstant.getCommentDetail(commentId);
            String CommentChildDetailKey = CommentContentKeyConstant.getCommentChildDetail(commentId);
            boolean isExist = redisTemplate.hasKey(CommentDetailKey) || redisTemplate.hasKey(CommentChildDetailKey);
            if (!isExist) {
                // 都没有就数据库
                CommentDo commentDo = commentDoMapper.selectByPrimaryKey(commentId);
                if (Objects.isNull(commentDo)) {
                    throw new BusinessException(ResponseStatusEnum.COMMENT_NOT_EXIST);
                }
            }
        }
    }

    private void getSecondCommentDataAndSync2Cache(List<FindSecondCommentItemRspVo> findSecondCommentItemRspVoList) {
        taskExecutor.submit(() -> {
            // todo 一级评论本地缓存
            Map<Long,String> localCacheData = Maps.newHashMap();
            findSecondCommentItemRspVoList.forEach(findCommentItemRspVo -> {
                Long commentId = findCommentItemRspVo.getCommentId();
                localCacheData.put(commentId,JsonUtil.toJsonString(findCommentItemRspVo));
            });
            LOCAL_CACHE.putAll(localCacheData);
        });
    }

    private void setSecondCommentCountData(List<FindSecondCommentItemRspVo> findSecondCommentItemRspVoList, List<Long> expiredCommentIds) {
        List<Long> notExpiredCommentIds = Lists.newArrayList();
        // 把没过期的评论id添加到notExpiredCommentIds
        findSecondCommentItemRspVoList.forEach(findCommentItemRspVo -> {
            Long leve1CommentId = findCommentItemRspVo.getCommentId();
            notExpiredCommentIds.add(leve1CommentId);
        });

        Map<Long, Map<Object, Object>> commentIdAndCountMap = getCommentCountDataAndSync2RedisHash(notExpiredCommentIds);

        for (FindSecondCommentItemRspVo findCommentItemRspVo : findSecondCommentItemRspVoList) {
            Long commentId = findCommentItemRspVo.getCommentId();

            if (CollUtil.isNotEmpty(commentIdAndCountMap) && expiredCommentIds.contains(commentId)) {
                continue;
            }

            Map<Object, Object> commentCountMap = commentIdAndCountMap.get(commentId);
            if (CollUtil.isNotEmpty(commentCountMap)) {
                findCommentItemRspVo.setLikeTotal(Long.valueOf(commentCountMap.get(CountConentRedisKeyConstant.LIKE_TOTAL).toString()));
            }
        }
    }

    private Map<Long, Map<Object, Object>> getCommentCountDataAndSync2RedisHash(List<Long> notExpiredCommentIds) {
        // 已失效的 Hash 评论 ID
        List<Long> expiredCommentIds = Lists.newArrayList();
        // 对于没有失效的评论id，在Set中查询计数
        List<String> commentZsetKeys = notExpiredCommentIds.stream()
                .map(CountConentRedisKeyConstant::getCountCommentKeyPrefix)
                .toList();
        // 使用 RedisTemplate 执行管道批量操作
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                // 遍历需要查询的评论计数的 Hash 键集合
                commentZsetKeys.forEach(key ->
                        // 在管道中执行 Redis 的 hash.entries 操作
                        // 此操作会获取指定 Hash 键中所有的字段和值
                        operations.opsForHash().entries(key));
                return null;
            }
        });

        Map<Long, Map<Object, Object>> commentIdAndCountMap = Maps.newHashMap();

        // 遍历未过期的评论 ID 集合
        for (int i = 0; i < notExpiredCommentIds.size(); i++) {
            // 当前评论 ID
            Long currCommentId = Long.valueOf(notExpiredCommentIds.get(i).toString());
            // 从缓存查询结果中，获取对应 Hash
            Map<Object, Object> hash = (Map<Object, Object>) results.get(i);
            // 若 Hash 结果为空，说明缓存中不存在，添加到 expiredCountCommentIds 中，保存一下
            if (CollUtil.isEmpty(hash)) {
                expiredCommentIds.add(currCommentId);
                continue;
            }
            // 若存在，则将数据添加到 commentIdAndCountMap 中，方便后续读取
            commentIdAndCountMap.put(currCommentId, hash);
        }

        // 若已过期的计数评论 ID 集合大于 0，说明部分计数数据不在 Redis 缓存中
        // 需要查询数据库，并将这部分的评论计数 Hash 同步到 Redis 中
        if (CollUtil.size(expiredCommentIds) > 0) {
            List<CommentDo> commentDos = commentDoMapper.selectCommentCountByIds(expiredCommentIds);

            // 遍历commentDos将结果放到字典里，要自己拿结构放进去
            commentDos.forEach(commentDo -> {
                Integer level = commentDo.getLevel();
                Map<Object,Object> map = Maps.newHashMap();
                map.put(CountConentRedisKeyConstant.LIKE_TOTAL,commentDo.getLikeTotal()); // likeTotal
                // 只有一级评论有子评论数
                if (Objects.equals(level, CommentLevelEnum.FIRST_LEVEL.getCode())) {
                    map.put(CountConentRedisKeyConstant.CHILD_COMMENT_TOTAL,commentDo.getChildCommentTotal()); // childCommentTotal
                }
                commentIdAndCountMap.put(commentDo.getId(),map);
            });

            // 由于已经过期了，在放完之后，需要更新到redis
            taskExecutor.execute(() -> {
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        commentDos.forEach((commentDo) -> {
                            Integer level = commentDo.getLevel();
                            String commentZsetKey = CountConentRedisKeyConstant.getCountCommentKeyPrefix(commentDo.getId());
                            Map<String,Long> ZSetFields = Maps.newHashMap();
                            if (Objects.equals(level, CommentLevelEnum.FIRST_LEVEL.getCode())) {
                                ZSetFields.put(CountConentRedisKeyConstant.CHILD_COMMENT_TOTAL,commentDo.getChildCommentTotal());
                            }
                            ZSetFields.put(CountConentRedisKeyConstant.LIKE_TOTAL,commentDo.getLikeTotal());
                            operations.opsForHash().putAll(commentZsetKey,ZSetFields);

                            long expireTime = 60 * 60 + RandomUtil.randomInt(60 * 60); // 过期时间为1小时 + 一小时随机数
                            operations.expire(commentZsetKey,expireTime, TimeUnit.SECONDS);
                        });
                        return null;
                    }
                });
            });
        }
        return commentIdAndCountMap;
    }

    private void getSecondCommentDataAndSync2Redis(List<CommentDo> commentDoList, List<FindSecondCommentItemRspVo> findSecondCommentItemRspVoList) {
        // 调用 KV 服务需要的入参
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        // 调用用户服务的入参
        List<Long> userIds = Lists.newArrayList();
        // 因为只有二级评论，不需要合并
        Long contentId = null;
        for(CommentDo commentDo : commentDoList) {
            contentId = commentDo.getContentId();
            if (!commentDo.getIsContentEmpty()) {
                // 添加 KV 服务需要的入参
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .yearMonth(commentDo.getCreateTime().format(DateTimeFormatter.ofPattern(DateConstants.YEAR_MONTH_PATTERN)))
                        .commentId(commentDo.getContentUuid())
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }
            // 添加用户服务需要的入参
            userIds.add(commentDo.getUserId());

            Long replyCommentId = commentDo.getReplyCommentId();
            Long parentId = commentDo.getParentId();
            if (!Objects.equals(replyCommentId, parentId) && !Objects.equals(commentDo.getReplyUserId(),0L)) {
                // 回复的评论与父级评论不一致，则需要查询回复的评论
                userIds.add(commentDo.getReplyUserId());
            }
        };
        // 调用 KV 服务，批量获取评论内容
        List<FindCommentContentRspDTO> findCommentContentRspDTOS = kvFeignApiService.QueryCommentContentList(contentId, findCommentContentReqDTOS);
        // DTO 集合转 Map, 方便后续拼装数据
        Map<String, String> commentContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
            commentContentMap = findCommentContentRspDTOS.stream()
                    .collect(Collectors.toMap(FindCommentContentRspDTO::getCommentId, FindCommentContentRspDTO::getComment));
        }
        // 调用用户服务，批量获取用户信息（头像、昵称等）
        List<FindUsersByIdResDTO> findUsersByIdResDTOS = userFeignApiService.findUsersById(userIds);
        // DTO 集合转 Map, 方便后续拼装数据
        Map<Long, FindUsersByIdResDTO> userInfoMap = null;
        if (CollUtil.isNotEmpty(findUsersByIdResDTOS)) {
            userInfoMap = findUsersByIdResDTOS.stream()
                    .collect(Collectors.toMap(FindUsersByIdResDTO::getId, findUsersByIdResDTO -> findUsersByIdResDTO));
        }
        for (CommentDo commentDo : commentDoList) {
            Long userId = commentDo.getUserId();
            FindSecondCommentItemRspVo findSecondCommentItemRspVo = FindSecondCommentItemRspVo.builder()
                    .commentId(commentDo.getId())
                    .userId(userId)
                    .imageUrl(commentDo.getImageUrl())
                    .likeTotal(commentDo.getLikeTotal())
                    .createTime(DateUtil.formatRelativeTime(commentDo.getCreateTime()))
                    .updateTime(commentDo.getUpdateTime())
                    .build();
            if (CollUtil.isNotEmpty(commentContentMap)) {
                String uuid = commentDo.getContentUuid();
                findSecondCommentItemRspVo.setComment(commentContentMap.get(uuid)); // 评论内容
            }
            if (CollUtil.isNotEmpty(userInfoMap)) {
                FindUsersByIdResDTO firstReplyUser = userInfoMap.get(userId);
                if (Objects.nonNull(firstReplyUser)) {
                    findSecondCommentItemRspVo.setNickname(firstReplyUser.getNickname()); // 昵称
                    findSecondCommentItemRspVo.setAvatar(firstReplyUser.getAvatar()); // 头像
                }
                Long parentId = commentDo.getParentId();
                Long replyCommentId = commentDo.getReplyCommentId();
                if (Objects.nonNull(replyCommentId) && !Objects.equals(replyCommentId, parentId)) {
                    Long replyUserId = commentDo.getReplyUserId();
                    FindUsersByIdResDTO replyUser = userInfoMap.get(replyUserId);
                    findSecondCommentItemRspVo.setReplyUserName(replyUser.getNickname()); // 回复用户昵称
                    findSecondCommentItemRspVo.setReplyUserId(replyUserId); // 回复用户 ID
                }
            }
            findSecondCommentItemRspVoList.add(findSecondCommentItemRspVo);
        }

        // 异步更新
        taskExecutor.execute(() -> {
            Map<String, String> map = Maps.newHashMap();
            findSecondCommentItemRspVoList.forEach(findSecondCommentItemRspVo -> {
                Long commentId = findSecondCommentItemRspVo.getCommentId();
                String redisKey = CommentContentKeyConstant.getCommentChildDetail(commentId);
                map.put(redisKey, JsonUtil.toJsonString(findSecondCommentItemRspVo));
            });
            batchAddComment2Redis(map); // 更新到redis
        });
    }

    private void batchAddComment2Redis(Map<String, String> map) {
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String JsonString = JsonUtil.toJsonString(entry.getValue());
                long expireTime = 60*60 + RandomUtil.randomInt(60);
                connection.setEx(
                        redisTemplate.getStringSerializer().serialize(entry.getKey()),
                        expireTime,
                        redisTemplate.getStringSerializer().serialize(JsonString));
            }
            return null;
        });
    }

    private void asyncSetCommentChildList(String commentChildZSetKey, Long level1CommentId) {
        List<CommentDo> commentDoList = commentDoMapper.selectChildCommentsByParentIdAndLimit(level1CommentId,  70);
        if (CollUtil.isNotEmpty(commentDoList)) {
            redisTemplate.executePipelined((RedisCallback<Object>) (connection) -> {
                ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
                commentDoList.forEach(comment -> {
                    Long commentTimestamp = DateUtil.LocalTimestampToDate(comment.getCreateTime());
                    zSetOperations.add(commentChildZSetKey, comment.getId(), commentTimestamp);
                });
                long exprieTime = 60*60 + RandomUtil.randomInt(60);
                redisTemplate.expire(commentChildZSetKey, exprieTime, TimeUnit.SECONDS);
                return null;
            });
        }
    }

    private void asyncSetChildCommentTotal(String commentZsetKey, Long count) {
        redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.opsForHash().put(commentZsetKey, CountConentRedisKeyConstant.CHILD_COMMENT_TOTAL, count);
                long expireTime = 60*60 + RandomUtil.randomInt(60); // 过期时间为1小时 + 随机数（0-60）秒
                operations.expire(commentZsetKey, expireTime, TimeUnit.SECONDS);
                return null;
            }
        });
    }

    /**
     * 设置评论计数数据
     * @param findCommentItemRspVoList
     * @param expiredCommentIds
     */
    private void setCommentCountData(List<FindCommentItemRspVo> findCommentItemRspVoList, List<Long> expiredCommentIds){
        List<Long> notExpiredCommentIds = Lists.newArrayList();
        // 把没过期的评论id添加到notExpiredCommentIds
        findCommentItemRspVoList.forEach(findCommentItemRspVo -> {
            Long leve1CommentId = findCommentItemRspVo.getCommentId();
            notExpiredCommentIds.add(leve1CommentId);
            FindCommentItemRspVo level2CommentItemRspVo = findCommentItemRspVo.getFirstReplyComment();
            if (Objects.nonNull(level2CommentItemRspVo)) {
                Long leve2CommentId = level2CommentItemRspVo.getCommentId();
                notExpiredCommentIds.add(leve2CommentId);
            }
        });


        Map<Long,Map<Object,Object>> commentCountMaps = getCommentCountDataAndSync2RedisHash(notExpiredCommentIds);

        // 更新完redis后，将对应的字段加到返参中
        for (FindCommentItemRspVo findCommentItemRspVo : findCommentItemRspVoList) {
            Long commentId = findCommentItemRspVo.getCommentId();
            if (CollUtil.isNotEmpty(expiredCommentIds) && expiredCommentIds.contains(commentId)) {
                // 若当前这条评论是从数据库中查询出来的, 则无需设置二级评论数、点赞数，以数据库查询出来的为主
                continue;
            }
            Map<Object,Object> commentCountMap = commentCountMaps.get(commentId);
            if (CollUtil.isNotEmpty(commentCountMap)) {
                Object likeToTalValue = commentCountMap.get(CountConentRedisKeyConstant.LIKE_TOTAL);
                Long likeTotal = likeToTalValue == null ? 0 : Long.parseLong(String.valueOf(likeToTalValue));
                Object childCommentTotalValue = commentCountMap.get(CountConentRedisKeyConstant.CHILD_COMMENT_TOTAL);
                Long childCommentTotal = childCommentTotalValue == null ? 0 : Long.parseLong(String.valueOf(childCommentTotalValue));
                findCommentItemRspVo.setLikeTotal(likeTotal);
                findCommentItemRspVo.setChildrenCommentTotal(childCommentTotal);

                // 二级评论放点赞数
                FindCommentItemRspVo firstReplyComment = findCommentItemRspVo.getFirstReplyComment();
                if (Objects.nonNull(firstReplyComment)) {
                    Long replyComment = firstReplyComment.getCommentId();
                    Map<Object,Object> replyCommentCountMap = commentCountMaps.get(replyComment);
                    if (CollUtil.isNotEmpty(replyCommentCountMap)) {
                        Object replyLikeToTalValue = replyCommentCountMap.get(CountConentRedisKeyConstant.LIKE_TOTAL);
                        Long replyLikeTotal = replyLikeToTalValue == null ? 0 : Long.parseLong(String.valueOf(replyLikeToTalValue));
                        firstReplyComment.setLikeTotal(replyLikeTotal);
                    }
                }
            }
        }
    };

    private void getCommentDataAndSync2Cache(List<FindCommentItemRspVo> findCommentItemRspVoList) {
        taskExecutor.submit(() -> {
            // todo 一级评论本地缓存
            Map<Long,String> localCacheData = Maps.newHashMap();
            findCommentItemRspVoList.forEach(findCommentItemRspVo -> {
                Long commentId = findCommentItemRspVo.getCommentId();
                localCacheData.put(commentId,JsonUtil.toJsonString(findCommentItemRspVo));
            });
            LOCAL_CACHE.putAll(localCacheData);
        });
    }

    /**
     * 拼接二级评论，添加到返参集合中
     * @param commentDoList 一级评论列表
     * @param contentId 内容 ID
     * @param findCommentItemRspVoList 返参集合
     */
    private void getCommentDataAndSync2Redis(List<CommentDo> commentDoList, Long contentId, List<FindCommentItemRspVo> findCommentItemRspVoList) {
        // 过滤出所有最早回复的二级评论 ID
        List<Long> firstReplyCommentIdList = commentDoList.stream()
                .map(CommentDo::getFirstReplyCommentId)
                .filter(FirstReplyCommentId-> FirstReplyCommentId != 0)
                .toList();
        // 查询二级评论
        Map<Long, CommentDo> firstReplyCommentMap = null;
        List<CommentDo> secondReplyCommentList = null;
        if (CollUtil.isNotEmpty(firstReplyCommentIdList)) {
            secondReplyCommentList = commentDoMapper.selectTwoLevelCommentByIds(firstReplyCommentIdList);
            firstReplyCommentMap = secondReplyCommentList.stream()
                    .collect(Collectors.toMap(CommentDo::getId, commentDo -> commentDo));
        }
        // 调用 KV 服务需要的入参
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        // 调用用户服务的入参
        List<Long> userIds = Lists.newArrayList();
        // 将一级评论和二级评论合并到一起
        List<CommentDo> allCommentDos = Lists.newArrayList();
        CollUtil.addAll(allCommentDos, commentDoList);
        CollUtil.addAll(allCommentDos, secondReplyCommentList);

        // 循环提取 RPC 调用需要的入参数据
        allCommentDos.forEach(commentDo -> {
//                log.info("commentDo: {}", commentDo);
            if (!commentDo.getIsContentEmpty()) {
                // 添加 KV 服务需要的入参
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .yearMonth(commentDo.getCreateTime().format(DateTimeFormatter.ofPattern(DateConstants.YEAR_MONTH_PATTERN)))
                        .commentId(commentDo.getContentUuid())
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }
            // 添加用户服务需要的入参
            userIds.add(commentDo.getUserId());
        });

        // 调用 KV 服务，批量获取评论内容
        List<FindCommentContentRspDTO> findCommentContentRspDTOS = kvFeignApiService.QueryCommentContentList(contentId, findCommentContentReqDTOS);
        // DTO 集合转 Map, 方便后续拼装数据
        Map<String, String> commentContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
            commentContentMap = findCommentContentRspDTOS.stream()
                    .collect(Collectors.toMap(FindCommentContentRspDTO::getCommentId, FindCommentContentRspDTO::getComment));
        }
        // 调用用户服务，批量获取用户信息（头像、昵称等）
        List<FindUsersByIdResDTO> findUsersByIdResDTOS = userFeignApiService.findUsersById(userIds);
        // DTO 集合转 Map, 方便后续拼装数据
        Map<Long, FindUsersByIdResDTO> userInfoMap = null;
        if (CollUtil.isNotEmpty(findUsersByIdResDTOS)) {
            userInfoMap = findUsersByIdResDTOS.stream()
                    .collect(Collectors.toMap(FindUsersByIdResDTO::getId, findUsersByIdResDTO -> findUsersByIdResDTO));
        }
        // DO 转 VO, 组合拼装一二级评论数据
        for (CommentDo commentDo : commentDoList) {
//                log.info("一级评论：{}", commentDo);
            // 一级评论
            Long userId = commentDo.getUserId();
            FindCommentItemRspVo firstCommentItemRspVo = FindCommentItemRspVo.builder()
                    .commentId(commentDo.getId())
                    .userId(userId)
                    .imageUrl(commentDo.getImageUrl())
                    .likeTotal(commentDo.getLikeTotal())
                    .createTime(DateUtil.formatRelativeTime(commentDo.getCreateTime()))
                    .updateTime(commentDo.getUpdateTime())
                    .childrenCommentTotal(commentDo.getChildCommentTotal()) // 子评论数
                    .heat(commentDo.getHeat())
                    .build();
            setCommentcontent(firstCommentItemRspVo, commentDo, commentContentMap);
            setUserInfo(userInfoMap,userId,firstCommentItemRspVo);

            // 二级评论
            Long firstReplyCommentId = commentDo.getFirstReplyCommentId();
            if (CollUtil.isNotEmpty(commentContentMap) && Objects.nonNull(firstReplyCommentMap)) {
                CommentDo firstReplyCommentUser = firstReplyCommentMap.get(firstReplyCommentId);
                if (Objects.nonNull(firstReplyCommentUser)) {
                    Long firstReplyUserId = firstReplyCommentUser.getUserId();
                    FindCommentItemRspVo firstReplyCommentItemRspVo = FindCommentItemRspVo.builder()
                            .commentId(firstReplyCommentId)
                            .userId(firstReplyUserId)
                            .imageUrl(firstReplyCommentUser.getImageUrl())
                            .likeTotal(firstReplyCommentUser.getLikeTotal())
                            .createTime(DateUtil.formatRelativeTime(firstReplyCommentUser.getCreateTime()))
                            .updateTime(firstReplyCommentUser.getUpdateTime())
                            .heat(firstReplyCommentUser.getHeat())
                            .build();
                    setCommentcontent(firstReplyCommentItemRspVo, firstReplyCommentUser, commentContentMap);
                    setUserInfo(userInfoMap,firstReplyUserId,firstReplyCommentItemRspVo);
                    // 将二级评论数据设置到一级评论中
                    firstCommentItemRspVo.setFirstReplyComment(firstReplyCommentItemRspVo);
                }
            }
//                log.info("一级评论拼接：{}",firstCommentItemRspVo);
            findCommentItemRspVoList.add(firstCommentItemRspVo);
        }

        // todo 异步将笔记详情，同步到 Redis 中
        taskExecutor.execute(() -> {
            // 准备批量写入的数据
            Map<String, String> commentMap = new HashMap<>();
            findCommentItemRspVoList.forEach(findCommentItemRspVo -> {
                Long commentId = findCommentItemRspVo.getCommentId();
                String commentKey = CommentContentKeyConstant.getCommentDetail(commentId);
                commentMap.put(commentKey, JsonUtil.toJsonString(findCommentItemRspVo));
            });

            // 使用 Redis Pipeline 提升写入性能
            batchAddComment2Redis(commentMap);
        });
    }

    /**
     * 同步热评数据到 Redis
     * @param commentZsetKey 热评 zset key
     * @param contentId 内容 ID
     */
    private void syncHotComment2Redis(String commentZsetKey, Long contentId) {
        List<CommentDo> commentDo = commentDoMapper.selectHotComments(contentId);
        if (CollUtil.isNotEmpty(commentDo)) {
            redisTemplate.executePipelined((RedisCallback<Object>) (connection) -> {
                ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
                commentDo.forEach(comment -> {
                    zSetOperations.add(commentZsetKey, comment.getId(), comment.getHeat());
                });
                long exprieTime = 60*60 + RandomUtil.randomInt(60);
                redisTemplate.expire(commentZsetKey, exprieTime, TimeUnit.SECONDS);
                return null;
            });
        }
    }

    /**
     * 异步设置评论数
     * @param countContentRedisKey 评论数缓存的 key
     * @param count 评论数
     */
    private void asyncSetCommentCount(String countContentRedisKey, Long count) {
        redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.opsForHash().put(countContentRedisKey, CountConentRedisKeyConstant.COMMENT_TOTAL, count);
                long expireTime = 60*60*23 + RandomUtil.randomInt(60); // 过期时间为1小时 + 随机数（0-60）秒
                operations.expire(countContentRedisKey, expireTime, TimeUnit.SECONDS);
                return null;
            }
        });
    }

    /**
     * 设置用户信息
     * @param userInfoMap 用户信息
     * @param userId 用户ID
     * @param firstCommentItemRspVo 当前级别评论
     */
    private void setUserInfo(Map<Long, FindUsersByIdResDTO> userInfoMap, Long userId, FindCommentItemRspVo firstCommentItemRspVo) {
        FindUsersByIdResDTO firstReplyUser = userInfoMap.get(userId);
        if (Objects.nonNull(firstReplyUser)) {
            firstCommentItemRspVo.setNickname(firstReplyUser.getNickname());
            firstCommentItemRspVo.setAvatar(firstReplyUser.getAvatar());
        }
    }

    /**
     * 设置评论内容
     * @param firstCommentItemRspVo 当前级别评论
     * @param commentDo 评论DO
     * @param commentContentMap 评论内容Map
     */
    private void setCommentcontent(FindCommentItemRspVo firstCommentItemRspVo, CommentDo commentDo, Map<String, String> commentContentMap) {
        if (CollUtil.isNotEmpty(commentContentMap)) {
            String uuid = commentDo.getContentUuid();
            firstCommentItemRspVo.setComment(commentContentMap.get(uuid));
        }
    }
}
