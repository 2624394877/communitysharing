package com.taoxin.communitysharing.comment.business.service.implement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.KV.dto.request.FindCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.comment.business.constant.CommentContentKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.CountConentRedisKeyConstant;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.comment.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.comment.business.model.dto.PublishCommentMqDTO;
import com.taoxin.communitysharing.comment.business.model.vo.req.CommentPublishReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.req.FindCommentPageListReqVo;
import com.taoxin.communitysharing.comment.business.model.vo.res.FindCommentItemRspVo;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

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

    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 初始化缓存大小
            .maximumSize(10000) // 最大缓存数量
            .expireAfterWrite(1, TimeUnit.HOURS) // 缓存过期时间
            .build(); // 创建缓存对象

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
                        findCommentItemRspVoList.add(JsonUtil.parseObject(commentDetailJson, FindCommentItemRspVo.class));
                    }
                }

                // 若 localCacheCommentIdsExist 大小等于 0，说明评论详情数据都在本地缓存中，直接响应返参
                if (Objects.equals(CollUtil.size(localCacheCommentIdsExist), 0)) {
                    return PageResponse.success(findCommentItemRspVoList, pageNo, totalCount, pageSize);
                }

                // 构建 MGET 批量查询评论详情的 Key 集合
                List<String> commentDetailKeys = localCacheCommentIdsExist.stream()
                        .map(CommentContentKeyConstant::getCommentDetail)
                        .toList();
                log.info("commentDetailKeys: {}", commentDetailKeys);
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
                        expiredCommentIds.add(Long.valueOf(commentIdList.get(i).toString()));
                    }
                }

                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    // 对于不存在的一级评论，需要批量从数据库中查询，并添加到 commentRspVOS 中
                    List<CommentDo> commentDoList = commentDoMapper.selectByCommentIds(expiredCommentIds);
                    getCommentDataAndSync2Redis(commentDoList, contentId, findCommentItemRspVoList);
                }
            }
            // todo 根据拿到的id表去redis中拿数据，并返回
            if (CollUtil.isNotEmpty(findCommentItemRspVoList)) {
                log.info("findCommentItemRspVoList: {}", findCommentItemRspVoList);
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

        // 缓存不存在，则从数据库中查询
        // 查询一级评论
        List<CommentDo> commentDoList = commentDoMapper.selectPageList(contentId, offset, pageSize);
//        log.info("一级评论：{}", commentDoList);
        getCommentDataAndSync2Redis(commentDoList, contentId, findCommentItemRspVoList);
        getCommentDataAndSync2Cache(findCommentItemRspVoList); // 本地缓存一级评论
        return PageResponse.success(findCommentItemRspVoList, pageNo, totalCount, pageSize);
    }

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
            log.info("secondReplyCommentList: {}", secondReplyCommentList);
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
            if (CollUtil.isNotEmpty(commentContentMap)) {
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
            redisTemplate.executePipelined((RedisCallback<?>)(connection)->{
                for (Map.Entry<String, String> entry : commentMap.entrySet()) {
                    String JsonString = JsonUtil.toJsonString(entry.getValue());
                    long expireTime = 60*60 + RandomUtil.randomInt(60);
                    connection.setEx(
                            redisTemplate.getStringSerializer().serialize(entry.getKey()),
                            expireTime,
                            redisTemplate.getStringSerializer().serialize(JsonString)
                    );
                }
                return null;
            });
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
                long expireTime = 60*60 + RandomUtil.randomInt(60); // 过期时间为1小时 + 随机数（0-60）秒
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
