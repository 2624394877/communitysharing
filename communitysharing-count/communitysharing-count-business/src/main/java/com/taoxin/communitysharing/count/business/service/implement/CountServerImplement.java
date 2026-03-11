package com.taoxin.communitysharing.count.business.service.implement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.count.business.constant.RedisKeyConstant;
import com.taoxin.communitysharing.count.business.domain.databaseObject.ContentCountDo;
import com.taoxin.communitysharing.count.business.domain.databaseObject.UserCountDo;
import com.taoxin.communitysharing.count.business.domain.mapper.ContentCountDoMapper;
import com.taoxin.communitysharing.count.business.domain.mapper.UserCountDoMapper;
import com.taoxin.communitysharing.count.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.count.business.model.vo.req.FindContentCountByIdReqVo;
import com.taoxin.communitysharing.count.business.model.vo.req.FindUserCountsByIdReqVo;
import com.taoxin.communitysharing.count.business.model.vo.res.FindContentCountByIdResVo;
import com.taoxin.communitysharing.count.business.model.vo.res.FindUserCountsByIdResVo;
import com.taoxin.communitysharing.count.business.service.CountServer;
import com.taoxin.communitysharing.count.model.dto.Req.FindContentCountReqDTO;
import com.taoxin.communitysharing.count.model.dto.Res.FindContentCountResDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CountServerImplement implements CountServer {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserCountDoMapper userCountDoMapper;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;
    @Resource
    private ContentCountDoMapper contentCountDoMapper;
    @Override
    public Response<FindUserCountsByIdResVo> findUserCountsById(FindUserCountsByIdReqVo reqVo) {
        Long userId = reqVo.getUserId();
        log.info("查询用户ID为{}的统计数据", userId);
        // 构建实体
        FindUserCountsByIdResVo resVo = FindUserCountsByIdResVo.builder()
                .userId(userId)
                // 设置默认值，防止响应数据中缺少字段
                .fansTotal(0L)
                .followingTotal(0L)
                .contentTotal(0L)
                .likeTotal(0L)
                .collectTotal(0L)
                .build();
        // todo redis中查询
        String buildCountUserKey = RedisKeyConstant.buildCountUserKey(userId);
        List<Object> redisList = redisTemplate.opsForHash()
                .multiGet(buildCountUserKey, List.of(
                        RedisKeyConstant.FIELD_FANS_TOTAL,
                        RedisKeyConstant.FIELD_FOLLOWING_TOTAL,
                        RedisKeyConstant.CONTENT_COUNT_TOTAL,
                        RedisKeyConstant.FIELD_LIKE_TOTAL,
                        RedisKeyConstant.FIELD_COLLECT_TOTAL
                ));
        boolean angNull = redisList.stream().anyMatch(Objects::isNull);
        if (!angNull) {
            // 没有一个字段为空
            resVo.setFansTotal((long) (int) redisList.get(0));
            resVo.setFollowingTotal((long)(int) redisList.get(1));
            resVo.setContentTotal((long)(int) redisList.get(2));
            resVo.setLikeTotal((long)(int) redisList.get(3));
            resVo.setCollectTotal((long)(int) redisList.get(4));
        } else {
            // todo 数据库查询
            // 只要有一个字段为空 就从数据库查询
            UserCountDo userCountDo = userCountDoMapper.selectByUserId(userId);
            if (Objects.nonNull(userCountDo)) {
                resVo.setFansTotal(userCountDo.getFansTotal());
                resVo.setFollowingTotal(userCountDo.getFollowingTotal());
                resVo.setContentTotal(userCountDo.getContentTotal());
                resVo.setLikeTotal(userCountDo.getLikeTotal());
                resVo.setCollectTotal(userCountDo.getCollectTotal());
            } else {
                log.info("====【计数服务】====>用户ID为{}的统计数据不存在", userId);
                throw new BusinessException(ResponseStatusEnum.PARAMS_ERROR.getErrorCode(),"统计数据不存在");
            }
            // 存入redis
            // 异步同步
            asyncData2Redis(resVo,buildCountUserKey);
        }

        return Response.success(resVo);
    }

    @Override
    public Response<List<FindContentCountResDTO>> findContentCount(FindContentCountReqDTO reqVo) {
        List<Long> contentIds = reqVo.getContentId();
        List<FindContentCountResDTO> findContentCountResDTOs = Lists.newArrayList();
        // 缓存
        List<String> redisKeyList = contentIds.stream()
                .map(RedisKeyConstant::buildCountContentKey)
                .toList();
        List<Object> redisList = getCountHashesByPipelineFromRedis(redisKeyList);
        // 缓存中没有的
        List<Long> needQuerySqlId = Lists.newArrayList();
        for (int i = 0; i < contentIds.size(); i++) {
            Long contentId = contentIds.get(i);
            List<Integer> hashValue = (List<Integer>) redisList.get(i);

            Integer likeTotal = hashValue.get(0);
            Integer collectTotal = hashValue.get(1);
            Integer commentTotal = hashValue.get(2);

            // 只要一个是null 就从数据库查询
            if (Objects.isNull(likeTotal) || Objects.isNull(collectTotal) || Objects.isNull(commentTotal)) {
                needQuerySqlId.add(contentId);
            }

            FindContentCountResDTO findContentCountResDTO = FindContentCountResDTO.builder()
                    .contentId(contentId)
                    .likeTotal(Objects.isNull(likeTotal)? null:Long.valueOf(likeTotal))
                    .collectTotal(Objects.isNull(collectTotal)? null:Long.valueOf(collectTotal))
                    .commentTotal(Objects.isNull(commentTotal)? null:Long.valueOf(commentTotal))
                    .build();
            findContentCountResDTOs.add(findContentCountResDTO);
        }
        if(CollUtil.isEmpty(needQuerySqlId)) return Response.success(findContentCountResDTOs);
        // 数据库查询
        List<ContentCountDo> contentCountDos = contentCountDoMapper.selectByContentIdList(needQuerySqlId);
        if (CollUtil.isNotEmpty(contentCountDos)) {
            Map<Long, ContentCountDo> map = contentCountDos.stream()
                    .collect(Collectors.toMap(ContentCountDo::getContentId, contentCountDo -> contentCountDo));
            // 调用redis存入数据
            async2redis(findContentCountResDTOs, map);

            findContentCountResDTOs.forEach(findContentCountResDTO -> {
                Long contentId = findContentCountResDTO.getContentId();
                Long likeTotal = findContentCountResDTO.getLikeTotal();
                Long collectTotal = findContentCountResDTO.getCollectTotal();
                Long commentTotal = findContentCountResDTO.getCommentTotal();

                if (Objects.isNull(likeTotal))
                    findContentCountResDTO.setLikeTotal(map.get(contentId).getLikeTotal());
                if (Objects.isNull(collectTotal))
                    findContentCountResDTO.setCollectTotal(map.get(contentId).getCollectTotal());
                if (Objects.isNull(commentTotal))
                    findContentCountResDTO.setCommentTotal(map.get(contentId).getCommentTotal());
            });
        }
        // 更新缓存
        return Response.success(findContentCountResDTOs);
    }

    @Override
    public Response<FindContentCountByIdResVo> findContentCountsById(FindContentCountByIdReqVo reqVo) {
        Long contentId = Long.valueOf(reqVo.getContentId());
        String buildCountContentKey = RedisKeyConstant.buildCountContentKey(contentId);
        List<Object> redisList = redisTemplate.opsForHash().multiGet(buildCountContentKey,
                List.of(RedisKeyConstant.FIELD_LIKE_TOTAL,
                        RedisKeyConstant.FIELD_COLLECT_TOTAL,
                        RedisKeyConstant.FIELD_COMMENT_TOTAL
                ));
        Integer likeTotal = (Integer) redisList.get(0);
        Integer collectTotal = (Integer) redisList.get(1);
        Integer commentTotal = (Integer) redisList.get(2);
        if (!Objects.isNull(likeTotal) && !Objects.isNull(collectTotal) && !Objects.isNull(commentTotal))
            return Response.success(FindContentCountByIdResVo.builder()
                    .contentId(String.valueOf(contentId))
                    .likeTotal(Long.valueOf(likeTotal))
                    .collectTotal(Long.valueOf(collectTotal))
                    .commentTotal(Long.valueOf(commentTotal))
                    .build());
        ContentCountDo contentCountDo = contentCountDoMapper.selectByPrimaryKey(contentId);
        if (Objects.isNull(contentCountDo)) throw new BusinessException(ResponseStatusEnum.PARAMS_ERROR.getErrorCode(),"统计数据不存在");
        FindContentCountByIdResVo findContentCountByIdReqVo = FindContentCountByIdResVo.builder()
                .contentId(String.valueOf(contentId))
                .likeTotal(contentCountDo.getLikeTotal())
                .collectTotal(contentCountDo.getCollectTotal())
                .commentTotal(contentCountDo.getCommentTotal())
                .build();
        Map<String, Long> contentCountMap = Maps.newHashMap();
        if (Objects.nonNull(likeTotal)) contentCountMap.put(RedisKeyConstant.FIELD_LIKE_TOTAL, findContentCountByIdReqVo.getLikeTotal());
        if (Objects.nonNull(collectTotal)) contentCountMap.put(RedisKeyConstant.FIELD_COLLECT_TOTAL, findContentCountByIdReqVo.getCollectTotal());
        if (Objects.nonNull(commentTotal)) contentCountMap.put(RedisKeyConstant.FIELD_COMMENT_TOTAL, findContentCountByIdReqVo.getCommentTotal());
        redisTemplate.executePipelined(new SessionCallback() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.opsForHash().putAll(buildCountContentKey, contentCountMap);
                long expire = 60 * 60 * 12 + RandomUtil.randomLong(60 * 60 * 12);
                operations.expire(buildCountContentKey, expire, TimeUnit.SECONDS);
                return null;
            }
        });
        return Response.success(findContentCountByIdReqVo);
    }

    private void async2redis(List<FindContentCountResDTO> findContentCountResDTOs, Map<Long, ContentCountDo> map) {
        redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (FindContentCountResDTO findContentCountResDTO : findContentCountResDTOs) {
                    Long likeTotal = findContentCountResDTO.getLikeTotal();
                    Long collectTotal = findContentCountResDTO.getCollectTotal();
                    Long commentTotal = findContentCountResDTO.getCommentTotal();

                    if (Objects.nonNull(likeTotal) && Objects.nonNull(collectTotal) && Objects.nonNull(commentTotal)) continue;

                    Long contentId = findContentCountResDTO.getContentId();
                    String redisKey = RedisKeyConstant.buildCountContentKey(contentId);

                    Map<String, Long> contentCountMap = Maps.newHashMap();
                    ContentCountDo contentCountDo = map.get(contentId);
                    if (Objects.isNull(likeTotal))
                        contentCountMap.put(RedisKeyConstant.LIKE_TOTAL, contentCountDo.getLikeTotal());
                    if (Objects.isNull(collectTotal))
                        contentCountMap.put(RedisKeyConstant.FIELD_COLLECT_TOTAL, contentCountDo.getCollectTotal());
                    if (Objects.isNull(commentTotal))
                        contentCountMap.put(RedisKeyConstant.FIELD_COMMENT_TOTAL, contentCountDo.getCommentTotal());
                    operations.opsForHash().putAll(redisKey, contentCountMap);
                    long expire = 60 * 60 * 12 + RandomUtil.randomLong(60 * 60 * 12);
                    operations.expire(redisKey, expire, TimeUnit.SECONDS);
                }
                return null;
            }
        });
    }

    private List<Object> getCountHashesByPipelineFromRedis(List<String> redisKeyList) {
        return redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (String redisKey : redisKeyList) {
                    operations.opsForHash().multiGet(redisKey, List.of(
                            RedisKeyConstant.LIKE_TOTAL,
                            RedisKeyConstant.FIELD_COLLECT_TOTAL,
                            RedisKeyConstant.FIELD_COMMENT_TOTAL
                    ));
                }
                return null;
            }
        });
    }

    private void asyncData2Redis(FindUserCountsByIdResVo resVo, String buildCountUserKey) {
        taskExecutor.execute(() -> {
            try {
                Map<String, Long> map = Maps.newHashMap();
                map.put(RedisKeyConstant.FIELD_FANS_TOTAL, resVo.getFansTotal());
                map.put(RedisKeyConstant.FIELD_FOLLOWING_TOTAL, resVo.getFollowingTotal());
                map.put(RedisKeyConstant.CONTENT_COUNT_TOTAL, resVo.getContentTotal());
                map.put(RedisKeyConstant.FIELD_LIKE_TOTAL, resVo.getLikeTotal());
                map.put(RedisKeyConstant.FIELD_COLLECT_TOTAL, resVo.getCollectTotal());
                redisTemplate.opsForHash().putAll(buildCountUserKey, map);
                long expireTime = 60 * 60 * 2 + RandomUtil.randomInt(60 * 60 * 2);
                redisTemplate.expire(buildCountUserKey, expireTime, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("====【计数服务】====>用户ID为{}的统计数据存入redis失败",buildCountUserKey, e);
            }
        });
    }
}
