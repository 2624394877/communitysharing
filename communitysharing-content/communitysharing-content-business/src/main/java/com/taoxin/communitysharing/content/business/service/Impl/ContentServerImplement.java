package com.taoxin.communitysharing.content.business.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.uitl.NumberUtil;
import com.taoxin.communitysharing.content.business.model.vo.res.ContentPublishListItemResVo;
import com.taoxin.communitysharing.content.business.model.vo.res.ContentPublishListResVo;
import com.taoxin.communitysharing.content.business.model.vo.res.LikeCollectStatusJudgeResVo;
import com.taoxin.communitysharing.content.business.rpc.CountFeignApiServer;
import com.taoxin.communitysharing.count.api.CountFeignServer;
import com.taoxin.communitysharing.count.model.dto.Res.FindContentCountResDTO;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.content.business.constant.ContentDetailsKeyConstant;
import com.taoxin.communitysharing.content.business.constant.MQConstant;
import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentCollectionDo;
import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentDo;
import com.taoxin.communitysharing.content.business.domain.databaseObject.ContentLikeDo;
import com.taoxin.communitysharing.content.business.domain.mapper.ContentCollectionDoMapper;
import com.taoxin.communitysharing.content.business.domain.mapper.ContentDoMapper;
import com.taoxin.communitysharing.content.business.domain.mapper.ContentLikeDoMapper;
import com.taoxin.communitysharing.content.business.domain.mapper.TopicDoMapper;
import com.taoxin.communitysharing.content.business.enums.*;
import com.taoxin.communitysharing.content.business.model.dto.ContentCollectUnCollectMQDTO;
import com.taoxin.communitysharing.content.business.model.dto.ContentOperateMqDTO;
import com.taoxin.communitysharing.content.business.model.dto.LikeUnlikeMQDTO;
import com.taoxin.communitysharing.content.business.model.vo.ContentPublishVo;
import com.taoxin.communitysharing.content.business.model.vo.req.*;
import com.taoxin.communitysharing.content.business.model.vo.res.ContentDetailsResVo;
import com.taoxin.communitysharing.content.business.rpc.IdConstructorFeignApiService;
import com.taoxin.communitysharing.content.business.rpc.KVFeignApiService;
import com.taoxin.communitysharing.content.business.rpc.UserFeignApiService;
import com.taoxin.communitysharing.content.business.service.ContentServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.taoxin.communitysharing.content.business.enums.ContentCollectTypeEnum.*;

@Service
@Slf4j
public class ContentServerImplement implements ContentServer {
    @Resource
    private KVFeignApiService kvFeignApiService;
    @Resource
    private ContentDoMapper contentDoMapper;
    @Resource
    private TopicDoMapper topicDoMapper;
    @Resource
    private ContentLikeDoMapper contentLikeDoMapper;
    @Resource
    private ContentCollectionDoMapper contentCollectionDoMapper;
    @Resource
    private IdConstructorFeignApiService idConstructorFeignApiService;
    @Resource
    private UserFeignApiService userFeignApiService;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private CountFeignApiServer countFeignServer;

    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000)
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    @Override
    public Response<?> PublishContent(ContentPublishVo contentPublishVo) {
        // 获取用户id
        Long userId = LoginUserContextHolder.getUserId();
        if (Objects.isNull(userId)) {
            throw new BusinessException(ResponseStatusEnum.PATH_ERROR);
        }
        // 获取类型
        Integer type = contentPublishVo.getType();
        // 获取对应类型的枚举
        ContentTypeEnum contentTypeEnum = ContentTypeEnum.getEnum(type);
        // 判断枚举是否合法
        if (Objects.isNull(contentTypeEnum)) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_TYPE_UNDIFF);
        }
        // 构建对应实体
        String imgUris = null;
        String videoUris = null;
        String link = null;
        String fileUris = null;
        boolean isContentEmpty = true;
        switch (contentTypeEnum) {
            case IMAGE: // 图文
                List<String> imgUris_list = contentPublishVo.getImgUris(); // 获取图片列表
                // 校验空数组
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUris_list), "图片列表不能为空"); // 判断数组是否为空
                // 校验图片数量
                Preconditions.checkArgument(imgUris_list.size() <= 10, "图片数量不能超过10张");
                // 拼接数组
                imgUris = StringUtils.join(imgUris_list, ",");
                break;
            case VIDEO: // 音视频
                List<String> videoUris_list = contentPublishVo.getVideoUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(videoUris_list), "视频列表不能为空");
                Preconditions.checkArgument(videoUris_list.size() <= 3, "视频数量不能超过3个");
                videoUris = StringUtils.join(videoUris_list, ",");
                break;
            case LINK: // 链接
                List<String> link_list = contentPublishVo.getLinkUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(link_list), "链接列表不能为空");
                link = StringUtils.join(link_list, ",");
                break;
            case FILE: // 文件
                List<String> file_list = contentPublishVo.getFileUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(file_list), "文件列表不能为空");
                Preconditions.checkArgument(file_list.size() <= 3, "文件数量不能超过5个");
                fileUris = StringUtils.join(file_list, ",");
                break;
        } // 对多媒体文件类型进行分支存储
        List<String> imgUris_list = contentPublishVo.getImgUris(); // 封面
        String cover = null;
        if (CollUtil.isNotEmpty(imgUris_list)) {
            cover = imgUris_list.get(0);
        }
        imgUris = cover;

        // 获取雪花ID
        String snowflakeIdId = idConstructorFeignApiService.getSnowflakeId();
        // 定义内容的id
        String contentUuid = null;
        // 获取内容
        String content = contentPublishVo.getContent();
        if (StringUtils.isNotBlank(content)) {
            isContentEmpty = false; // 内容不为空
            // 生成内容id
            contentUuid = UUID.randomUUID().toString();
            // 调用KV服务
            boolean addSharingContent = kvFeignApiService.addSharingContent(contentUuid, content);
            if (!addSharingContent) {
                throw new BusinessException(ResponseStatusEnum.CONTENT_PUBLISH_ERROR);
            }
        }

        // 获取话题id
        Long topicId = contentPublishVo.getTopicId();
        // 获取话题名称
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicDoMapper.getTopicNameByTopicId(topicId);
        }

        Long channelId = contentPublishVo.getChannelId();
        // 构建实体
        ContentDo contentDo = ContentDo.builder()
                .id(Long.parseLong(snowflakeIdId))
                .title(contentPublishVo.getTitle())
                .isContentEmpty(isContentEmpty)
                .creatorId(userId)
                .topicId(topicId)
                .topicName(topicName)
                .channelId(channelId)
                .isTop(Boolean.FALSE) // 默认不置顶
                .type(type)
                .imgUris(imgUris)
                .videoUri(videoUris)
                .fileUris(fileUris)
                .urlUris(link)
                .visible(ContentVisibleEnum.PUBLIC.getCode()) // 默认公开
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(ContentStatusEnum.NORMAL.getCode()) // 默认正常
                .contentUuid(contentUuid)
                .build();

        // 删除个人主页缓存
        String userHomePageCacheKey = ContentDetailsKeyConstant.getUserPublishContentListKey(userId);
        redisTemplate.delete(userHomePageCacheKey);
        // 插入数据库
        try {
            contentDoMapper.insertSelective(contentDo);
        } catch (Exception e) {
            log.error("发布内容失败", e);
            if (StringUtils.isNotBlank(contentUuid)) {
                kvFeignApiService.deleteSharingContent(contentUuid);
            }
        }
        //发送消息双删
        sendDelayMessage(userId);
        ContentOperateMqDTO contentOperateMqDTO = ContentOperateMqDTO.builder()
                .creatorId(userId)
                .contentId(Long.parseLong(snowflakeIdId))
                .operateType(ContentOperateEnum.PUBLISH.getCode())
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(contentOperateMqDTO))
                .build(); // 创建消息对象
        String topicTag = MQConstant.TOPIC_CONTENT_OPERATION_RECORD + ":" + MQConstant.TAG_CREATE_CONTENT;
        rocketMQTemplate.asyncSend(topicTag, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("内容发布记录发送MQ成功: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(contentOperateMqDTO));
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("内容发布记录发送MQ失败: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(contentOperateMqDTO), throwable);
            }
        }); // 发送消息
        return Response.success();
    }

    private void sendDelayMessage(Long userId) {
        Message<Long> message = MessageBuilder.withPayload(userId)
                .build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("====【内容发布】===>用户内容延迟删除发送MQ成功: {}, 延迟时间: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("====【内容发布】===>用户内容延迟删除发送MQ失败: {}", throwable);
            }
        }, 3000,1);
    }

    @Override
    public Response<ContentDetailsResVo> ContentDetails(ContentDetailsReqVo contentDetailsReqVo) {
        Long ContentId = Long.valueOf(contentDetailsReqVo.getId()); // 获取内容id
        Long Userid = LoginUserContextHolder.getUserId(); // 获取用户id
        // 从Caffeine中获取内容详情
        String contentDetailsResVoCache = LOCAL_CACHE.getIfPresent(ContentId);
        ContentDetailsResVo contentDetailsResVoCaffeine = null;
        if (StringUtils.isNotBlank(contentDetailsResVoCache)) {
            contentDetailsResVoCaffeine = JsonUtil.parseObject(contentDetailsResVoCache, ContentDetailsResVo.class);
            log.info("====> 从Caffeine中获取内容详情");
            if (Objects.nonNull(contentDetailsResVoCaffeine) && !contentDetailsResVoCaffeine.isVisible()) {
                throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
            }
            return Response.success(contentDetailsResVoCaffeine);
        }

        // 从redis中获取内容详情
        String redisDetailKey = ContentDetailsKeyConstant.getContentDetailsKey(ContentId);
        String contentDetail = redisTemplate.opsForValue().get(redisDetailKey);
        if (StringUtils.isNotBlank(contentDetail)) {
            ContentDetailsResVo contentDetailsResVo = JsonUtil.parseObject(contentDetail, ContentDetailsResVo.class);
            if (Objects.nonNull(contentDetailsResVo) && !contentDetailsResVo.isVisible()) {
                throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
            }
            // 异步线程中将用户信息存入本地缓存
            threadPoolTaskExecutor.submit(() -> {
                // 写入本地缓存
                LOCAL_CACHE.put(ContentId,
                        Objects.isNull(contentDetailsResVo) ? "null" : JsonUtil.toJsonString(contentDetailsResVo));
            });
            return Response.success(contentDetailsResVo);
        }

        ContentDo contentDo = contentDoMapper.selectByPrimaryKey(ContentId); // 查询内容
        if (Objects.isNull(contentDo)) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
        }
        Integer visible = contentDo.getVisible(); // 获取可见性
        // 校验可见性
        boolean isVisible = checkContentVisible(visible, contentDo.getCreatorId(), Userid);
        if (Objects.equals(isVisible,Boolean.FALSE)) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_VISIBLE);
        }
        /* 并发处理 */
        // 用户查询
        long creatorId = contentDo.getCreatorId();
        CompletableFuture<FindUserByIdResDTO> userResultFuture = CompletableFuture.supplyAsync(() -> userFeignApiService.getUserInfoById(creatorId));

        // content内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(contentDo.getIsContentEmpty(),Boolean.FALSE)) {
            contentResultFuture = CompletableFuture.supplyAsync(() -> kvFeignApiService.findSharingContent(contentDo.getContentUuid()));
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<ContentDetailsResVo> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                    FindUserByIdResDTO findUserByIdResDTO = userResultFuture.join();
                    String content = finalContentResultFuture.join();

                    // 笔记类型
                    Integer type = contentDo.getType();

                    // 图片
                    String imgUris = contentDo.getImgUris();
                    List<String> imgUris_list = null;
                    if (Objects.equals(type,ContentTypeEnum.IMAGE.getCode()) && StringUtils.isNotBlank(imgUris)) {
                        imgUris_list = Arrays.asList(imgUris.split(","));
                    }

                    // 视频
                    String videoUri = contentDo.getVideoUri();
                    List<String> videoUri_list = null;
                    if (Objects.equals(type,ContentTypeEnum.VIDEO.getCode()) && StringUtils.isNotBlank(videoUri)) {
                        videoUri_list = Arrays.asList(videoUri.split(","));
                    }

                    // 文件
                    String fileUris = contentDo.getFileUris();
                    List<String> fileUris_list = null;
                    if (Objects.equals(type,ContentTypeEnum.FILE.getCode()) && StringUtils.isNotBlank(fileUris)) {
                        fileUris_list = Arrays.asList(fileUris.split(","));
                    }

                    // 链接
                    String urlUris = contentDo.getUrlUris();
                    List<String> urlUris_list = null;
                    if (Objects.equals(type,ContentTypeEnum.LINK.getCode()) && StringUtils.isNotBlank(urlUris)) {
                        urlUris_list = Arrays.asList(urlUris.split(","));
                    }

                    // 构建详情返回对象
//                    ContentDetailsResVo contentDetailsResVo =
                      return ContentDetailsResVo.builder()
                            .id(ContentId)
                            .title(contentDo.getTitle())
                            .content(content)
                            .creatorId(String.valueOf(creatorId))
                            .creatorName(findUserByIdResDTO.getNickname())
                            .avatar(findUserByIdResDTO.getAvatar())
                            .type(contentDo.getType())
                            .topicId(contentDo.getTopicId())
                            .topicName(contentDo.getTopicName())
                            .imgUris(imgUris_list)
                            .videoUris(videoUri_list)
                            .fileUris(fileUris_list)
                            .linkUris(urlUris_list)
                            .updateTime(contentDo.getUpdateTime())
                            .visible(isVisible)
                            .build();
                });

        ContentDetailsResVo contentDetailsResVo = resultFuture.join();
        // 将内容详情数据存到redis中
        threadPoolTaskExecutor.submit(()->{
            long expireTime = 60*60*24 + RandomUtil.randomInt(60*60*24); // 随机过期时间
            redisTemplate.opsForValue().set(redisDetailKey, JsonUtil.toJsonString(contentDetailsResVo), expireTime, TimeUnit.SECONDS);
        });
        return Response.success(contentDetailsResVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> UpdateContent(ContentUpdateReqVo contentUpdateReqVo) {
        Long userId = LoginUserContextHolder.getUserId();
        Long ContentId = Long.valueOf(contentUpdateReqVo.getContentId());
        Long creatorId = contentDoMapper.selectByPrimaryKey(ContentId).getCreatorId();

        if (!Objects.equals(userId,creatorId)) throw new BusinessException(ResponseStatusEnum.CANT_UPDATE_YOURSELF_CONTENT);

        Integer type = contentUpdateReqVo.getType();
        ContentTypeEnum contentTypeEnum = ContentTypeEnum.getEnum(type);
        if (Objects.isNull(contentTypeEnum)) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_TYPE_UNDIFF);
        }
        String imgUris = null;
        String videoUris = null;
        String fileUris = null;
        String urlUris = null;

        List<String> imgUris_list = contentUpdateReqVo.getImgUris(); // 获取图片列表
        // 校验空数组
        Preconditions.checkArgument(CollUtil.isNotEmpty(imgUris_list), "至少一张需要封面图"); // 判断数组是否为空
        // 校验图片数量
        Preconditions.checkArgument(imgUris_list.size() <= 10, "图片数量不能超过10张");
        // 拼接数组
        imgUris = StringUtils.join(imgUris_list, ",");
        switch (contentTypeEnum) {
//            case IMAGE:
//                List<String> imgUris_list = contentUpdateReqVo.getImgUris(); // 获取图片列表
//                // 校验空数组
//                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUris_list), "图片列表不能为空"); // 判断数组是否为空
//                // 校验图片数量
//                Preconditions.checkArgument(imgUris_list.size() <= 10, "图片数量不能超过10张");
//                // 拼接数组
//                imgUris = StringUtils.join(imgUris_list, ",");
//                break;
            case VIDEO: // 音视频
                List<String> videoUris_list = contentUpdateReqVo.getVideoUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(videoUris_list), "视频列表不能为空");
                Preconditions.checkArgument(videoUris_list.size() <= 3, "视频数量不能超过3个");
                videoUris = StringUtils.join(videoUris_list, ",");
                break;
            case LINK: // 链接
                List<String> link_list = contentUpdateReqVo.getLinkUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(link_list), "链接列表不能为空");
                urlUris = StringUtils.join(link_list, ",");
                break;
            case FILE: // 文件
                List<String> file_list = contentUpdateReqVo.getFileUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(file_list), "文件列表不能为空");
                Preconditions.checkArgument(file_list.size() <= 3, "文件数量不能超过5个");
                fileUris = StringUtils.join(file_list, ",");
                break;
        }
        Long topicId = contentUpdateReqVo.getTopicId();
        // 获取话题名称
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicDoMapper.getTopicNameByTopicId(topicId);
            if (StringUtils.isBlank(topicName)) {
                throw new BusinessException(ResponseStatusEnum.TOPIC_NOT_EXIST);
            }
        }

        // 更新内容
        String content = contentUpdateReqVo.getContent();
        ContentDo contentSelect = contentDoMapper.selectByPrimaryKey(ContentId);
        log.info("更新内容: {}", contentSelect);
        if (Objects.isNull(contentSelect)) throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
        String content_uuid = contentSelect.getContentUuid();
        boolean content_update_flag = false;
        if (StringUtils.isBlank(content)) { // 内容为空
            content_update_flag = kvFeignApiService.deleteSharingContent(content_uuid);
        } else {
            content_uuid = StringUtils.isBlank(content_uuid) ? UUID.randomUUID().toString() : content_uuid;
            content_update_flag = kvFeignApiService.addSharingContent(content_uuid, content);
        }
        if (!content_update_flag) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_UPDATED);
        }

        // 删除redis中的内容详情数据
        String redisDetailKey = ContentDetailsKeyConstant.getContentDetailsKey(ContentId);
        String userHomePageCacheKey = ContentDetailsKeyConstant.getUserPublishContentListKey(userId);
        redisTemplate.delete(Arrays.asList(redisDetailKey, userHomePageCacheKey));

        // 可见性
        Integer visible = contentUpdateReqVo.getVisible() ? 0 : 1; // 0: 可见 1: 不可见
        ContentDo contentDo = ContentDo.builder()
                .id(ContentId)
                .title(contentUpdateReqVo.getTitle())
                .isContentEmpty(StringUtils.isBlank(content))
                .type(type)
                .imgUris(imgUris)
                .videoUri(videoUris)
                .fileUris(fileUris)
                .urlUris(urlUris)
                .topicId(topicId)
                .topicName(topicName)
                .updateTime(LocalDateTime.now())
                .visible(visible)
                .contentUuid(content_uuid)
                .build();
        contentDoMapper.updateByPrimaryKeySelective(contentDo);
        sendDelayMessageUpdate(Arrays.asList(userId, ContentId));
        // 延时双删+消息队列(异步)
        Message<Long> message = MessageBuilder.withPayload(ContentId)
                .build(); // 创建消息对象
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_DELAY_DELETE_CONTENT_LOCAL_CACHE, message,
                new SendCallback() {

                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("延时双删+消息队列(异步)发送成功: {}", ContentId);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("延时双删+消息队列(异步)发送失败: {}", ContentId);
                    }
                },
                3000, // 超时时间(毫秒)
                1 // 延迟级别
        ); // 发送消息

        // 同步发送MQ
        rocketMQTemplate.syncSend(MQConstant.TOPIC_DELETE_CONTENT_LOCAL_CACHE, ContentId);
        log.info("同步发送MQ(删除): {}", ContentId);

        // 删除本地数据
        LOCAL_CACHE.invalidate(ContentId);
        return Response.success();
    }

    private void sendDelayMessageUpdate(List<Long> list) {
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(list))
                .build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE_UPDATE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("====【内容修改】===>用户内容延迟删除发送MQ成功: {}, 延迟时间: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("====【内容修改】===>用户内容延迟删除发送MQ失败: {}", throwable);
            }
        }, 3000,1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> DeleteContent(ContentDeleteReqVo contentDeleteReqVo) {
        Long ContentId = Long.valueOf(contentDeleteReqVo.getContentId());
        Long userId = LoginUserContextHolder.getUserId();

        String redisDetailKey = ContentDetailsKeyConstant.getContentDetailsKey(ContentId);
        String userHomePageCacheKey = ContentDetailsKeyConstant.getUserPublishContentListKey(userId);
        redisTemplate.delete(Arrays.asList(redisDetailKey, userHomePageCacheKey));
        // 逻辑删除
        ContentDo contentDo = ContentDo.builder()
                .id(ContentId)
                .status(ContentStatusEnum.DELETED.getCode())
                .updateTime(LocalDateTime.now())
                .build();
        int count = contentDoMapper.updateByPrimaryKeySelective(contentDo);
        if (count <= 0) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
        }

        sendDelayMessageDelete(Arrays.asList(userId, ContentId));

        rocketMQTemplate.syncSend(MQConstant.TOPIC_DELETE_CONTENT_LOCAL_CACHE, ContentId);
        log.info("同步发送MQ(删除): {}", ContentId);
        ContentOperateMqDTO contentOperateMqDTO = ContentOperateMqDTO.builder()
                .creatorId(userId)
                .contentId(contentDo.getId())
                .operateType(ContentOperateEnum.DELETED.getCode())
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(contentOperateMqDTO))
                .build(); // 创建消息对象
        String topicTag = MQConstant.TOPIC_CONTENT_OPERATION_RECORD + ":" + MQConstant.TAG_DELETE_CONTENT;
        rocketMQTemplate.asyncSend(topicTag, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("内容删除记录发送MQ成功: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(contentOperateMqDTO));
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("内容删除记录发送MQ失败: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(contentOperateMqDTO), throwable);
            }
        }); // 发送消息
        return Response.success();
    }

    private void sendDelayMessageDelete(List<Long> list) {
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(list))
                .build();
        rocketMQTemplate.asyncSend(MQConstant.TOPIC_DELAY_DELETE_PUBLISHED_CONTENT_LIST_REDIS_CACHE_UPDATE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("====【内容删除】===>用户内容延迟删除发送MQ成功: {}, 延迟时间: {}", sendResult, 3000);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("====【内容删除】===>用户内容延迟删除发送MQ失败",throwable);
            }
        }, 3000,1);
    }

    @Override
    public void DeleteContentLocalCache(Long contentId) {
        LOCAL_CACHE.invalidate(contentId);
    }

    @Override
    public Response<?> SetContentPrivateOfVisible(ContentPrivateOfVisibleReqVo contentPrivateOfVisibleReqVo) {
        Long contentId = contentPrivateOfVisibleReqVo.getContentId();

        ContentDo contentDo = ContentDo.builder()
                .id(contentId)
                .visible(ContentVisibleEnum.PRIVATE.getCode())
                .updateTime(LocalDateTime.now())
                .build();
        int count = contentDoMapper.updatePrivateOfVisibleByPrimaryKey(contentDo);
        if (count <= 0) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_CANT_PRIVATE);
        }

        String redisDetailKey = ContentDetailsKeyConstant.getContentDetailsKey(contentId);
        redisTemplate.delete(redisDetailKey);

        rocketMQTemplate.syncSend(MQConstant.TOPIC_DELAY_DELETE_CONTENT_LOCAL_CACHE, contentId);
        log.info("同步发送MQ: {}", contentId);
        return Response.success("已设置为私有");
    }

    @Override
    public Response<?> SetContentIsTop(ContentIsTopReqVo contentIsTopReqVo) {
        Long contentId = contentIsTopReqVo.getContentId();
        Long userId = LoginUserContextHolder.getUserId();
        boolean isTop = contentIsTopReqVo.isTop();
        log.info("置顶: {}", isTop);
        ContentDo contentDo = ContentDo.builder()
                .id(contentId)
                .creatorId(userId)
                .isTop(isTop)
                .updateTime(LocalDateTime.now())
                .build();
        int count = contentDoMapper.updateIsTopByPrimaryKey(contentDo);
        if (count <= 0) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_TOP_FAIL);
        }

        String redisDetailKey = ContentDetailsKeyConstant.getContentDetailsKey(contentId);
        redisTemplate.delete(redisDetailKey);

        rocketMQTemplate.syncSend(MQConstant.TOPIC_DELETE_CONTENT_LOCAL_CACHE, contentId);
        log.info("同步发送MQ: {}", contentId);
        return isTop ? Response.success("已置顶") : Response.success("已取消置顶");
    }

    /**
     * 内容点赞
     * @param contentLikeReqVo 请求体
     * @return 成功响应或异常抛出
     */
    @Override
    public Response<?> ContentLike(ContentLikeReqVo contentLikeReqVo) {
        Long contentId = Long.valueOf(contentLikeReqVo.getContentId());
        // 1. 校验被点赞的笔记是否存在
        Long creator = checkContentIsExist(contentId);
        // 2. 判断目标笔记，是否已经点赞过
        Long userid = LoginUserContextHolder.getUserId();
        String bloomFilterKey = ContentDetailsKeyConstant.getBloomUserContentLikeListKey(userid);
        // 执行lua脚本，判断是否存在布隆过滤器中
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_content_like_check.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId);
        ContentLikeLuaResultEnum contentLikeLuaResultEnum = ContentLikeLuaResultEnum.valueOf(result);
        if (Objects.isNull(contentLikeLuaResultEnum)) {
            throw new BusinessException(ResponseStatusEnum.CONTENT_LIKE_ERROR);
        }
        String userContentLikedZsetKey = ContentDetailsKeyConstant.getBloomUserContentLikeZSetKey(userid);
        switch (contentLikeLuaResultEnum) {
            case NOT_EXIST -> { // 布隆过滤器不存在
                // todo 在数据库中查找，并初始化布隆过滤器
                // 查询数据库中，当前用户是否点赞过该内容
                int count = contentLikeDoMapper.selectCountByUserIdAndContentId(userid, contentId);

                long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数

                if (count > 0) {
                    // 表示点赞过 数据库查到了就要更新布隆过滤器
                    Long userId;
                    userId = userid;
//                    asyncBatchAddContentLikeToBloomFilter(userId,expireTime,bloomFilterKey);
//                    throw new BusinessException(ResponseStatusEnum.CONTENT_ALREADY_LIKED);

                    // 异步初始化 Roaring Bitmap
                    threadPoolTaskExecutor.submit(() ->
                            batchAddContentLike2R64BitmapAndExpire(userId,expireTime,bloomFilterKey));
                    throw new BusinessException(ResponseStatusEnum.CONTENT_ALREADY_LIKED);
                }

//                // 数据库中也没有数据，表还没点赞过，将id添加到布隆过滤器中
//                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_content_like_and_expire.lua")));
//                redisScript.setResultType(Long.class);
//                redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId,expireTime);
                batchAddContentLike2R64BitmapAndExpire(userid,expireTime,bloomFilterKey);
                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_add_content_like_and_expire.lua")));
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId,expireTime);

            }
            // id存在，提示已经点赞过了
            case BLOOM_LIKED -> {
                // 获取数据库中点赞的分数（时间戳）
                /*Double score = redisTemplate.opsForZSet().score(userContentLikedZsetKey, contentId);
                if (Objects.nonNull(score)) {
                    throw new BusinessException(ResponseStatusEnum.CONTENT_ALREADY_LIKED);
                }
                int count = contentLikeDoMapper.selectCountIsLiked(userid, contentId);
                if (count > 0) {
                    // 设置一个重新初始化ZSet的方法，防止过期删除后，频繁调用上方数据库查询的逻辑，导致数据库压力过大
                    Long userId;
                    userId = userid;
                    asyncInitContentLikedZSet(userId, userContentLikedZsetKey);
                    throw new BusinessException(ResponseStatusEnum.CONTENT_ALREADY_LIKED);
                }*/
                throw new BusinessException(ResponseStatusEnum.CONTENT_ALREADY_LIKED);
            }
        }
        // 3. 更新用户 ZSET 点赞列表
        LocalDateTime now = LocalDateTime.now();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/content_like_check_and_update_zset.lua")));
        redisScript.setResultType(Long.class);

        result = redisTemplate.execute(redisScript, Collections.singletonList(userContentLikedZsetKey), contentId, DateUtil.LocalTimestampToDate(now));

        if (Objects.equals(result, ContentLikeLuaResultEnum.NOT_EXIST.getCode())) {
            // todo redis列表不存在，在数据中查询，并重新初始化
            List<ContentLikeDo> contentLikeDoList = contentLikeDoMapper.selectLikedByUserIdAndLimit(userid,100);

            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数

            DefaultRedisScript<Long> redisScriptZSet = new DefaultRedisScript<>();
            redisScriptZSet.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_content_like_zset_and_expire.lua")));
            redisScriptZSet.setResultType(Long.class);

            if (CollUtil.isNotEmpty(contentLikeDoList)) {
                // 数据库中有数据，则批量添加
                Object[] luaArgs = getArgsForBatchAddContentLikeZSet(contentLikeDoList, expireTime);
                redisTemplate.execute(redisScriptZSet, Collections.singletonList(userContentLikedZsetKey), luaArgs);

                // 在调用一次，将点赞的内容id添加到zset中
                redisTemplate.execute(redisScript, Collections.singletonList(userContentLikedZsetKey), contentId, DateUtil.LocalTimestampToDate(now));
            } else {
                // 数据库中没有数据，直接添加当前点赞的内容id到zset中
                List<Object> luaArgs = Lists.newArrayList();
                // 插入的顺序：时间戳，内容id，过期时间
                luaArgs.add(DateUtil.LocalTimestampToDate(now));
                luaArgs.add(contentId);
                luaArgs.add(expireTime);
                redisTemplate.execute(redisScriptZSet, Collections.singletonList(userContentLikedZsetKey), luaArgs.toArray());
            }
        }
        // 4. 发送 MQ, 将点赞数据落库
        LikeUnlikeMQDTO likeUnlikeMQDTO = LikeUnlikeMQDTO.builder()
                .contentId(contentId)
                .userId(userid)
                .createTime(now)
                .status(LikeUnlikeContentTypeEnum.LIKE.getCode())
                .creatorId(creator)
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(likeUnlikeMQDTO))
                .build(); // 创建消息对象
        // 构建主题标签
        String topicTag = MQConstant.TOPIC_LIKE_OR_UNLIKE + ":" + MQConstant.TAG_LIKE;
        String hashkey = String.valueOf(contentId); // 以内容id作为hashkey，保证同一内容的点赞消息落到同一个队列中，保证顺序
        rocketMQTemplate.asyncSendOrderly(topicTag, message, hashkey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【内容点赞】内容点赞发送MQ成功: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(likeUnlikeMQDTO));
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【内容点赞】内容点赞发送MQ失败: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(likeUnlikeMQDTO), throwable);
            }
        }); // 发送消息
        return Response.success("点赞成功");
    }

    private void batchAddContentLike2R64BitmapAndExpire(Long userId, long expireTime, String bloomFilterKey) {
        try {
            List<ContentLikeDo> contentLikeDoList = contentLikeDoMapper.selectContentsIdByUserId(userId);
            if (CollUtil.isNotEmpty(contentLikeDoList)) {
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_batch_add_content_like_and_expire.lua")));
                redisScript.setResultType(Long.class);
                List<Object> LuaArgs = Lists.newArrayList();
                for (ContentLikeDo contentLikeDo : contentLikeDoList) {
                    LuaArgs.add(contentLikeDo.getContentId());
                }
                LuaArgs.add(expireTime);
                redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), LuaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("【内容点赞】批量添加用户点赞内容id添加到bitmap失败", e);
        }
    }

    @Override
    public Response<?> ContentUnlike(ContentUnlikeReqVo contentUnlikeReqVo) {
        Long contentId = Long.valueOf(contentUnlikeReqVo.getContentId());
        // 1. 校验被点赞的笔记是否存在
        Long creator = checkContentIsExist(contentId);
        // 2. 判断目标笔记，是否已经点踩过
        Long userid = LoginUserContextHolder.getUserId();
        String bloomFilterKey = ContentDetailsKeyConstant.getBloomUserContentLikeListKey(userid);
        // 执行lua脚本，判断是否存在布隆过滤器中
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_content_unlike_check.lua")));
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId);
        ContentUnlikeLuaResultEnum contentUnlikeLuaResultEnum = ContentUnlikeLuaResultEnum.valueOf(result);
        if (Objects.isNull(contentUnlikeLuaResultEnum)) throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
        switch (contentUnlikeLuaResultEnum) {
            case NOT_EXISTS -> {
                // 布隆过滤器不存在，查询数据库，初始化
                Long userId;
                userId = userid;
                threadPoolTaskExecutor.submit(()->{
                    long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数
                    batchAddContentLike2R64BitmapAndExpire(userId, expireTime, bloomFilterKey);
                });
                int count = contentLikeDoMapper.selectCountByUserIdAndContentId(userid, contentId);
                if (count <= 0) throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_LIKED);
            }
            // 布隆过滤器中不存在元素，提示没有点赞过，不能点踩(绝对正确)
            case UNLIKED -> throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_LIKED);
            /*case LIKED -> {
                // 点过赞，判断有没有取消点赞的记录，如果有，说明之前点过赞又取消了点赞，现在又来了一个取消点赞的请求，提示没有点赞过，不能点踩
                int count = contentLikeDoMapper.selectCountByUserIdAndContentId(userid, contentId);
                if (count <= 0) throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_LIKED);
            }*/
        }
        // 3. 更新用户 ZSET 点赞列表
        String userContentLikedZsetKey = ContentDetailsKeyConstant.getBloomUserContentLikeZSetKey(userid);
        redisTemplate.opsForZSet().remove(userContentLikedZsetKey, contentId);
        // 4. 发送 MQ, 将取消点赞数据落库
        LikeUnlikeMQDTO likeUnlikeMQDTO = LikeUnlikeMQDTO.builder()
                .userId(userid)
                .contentId(contentId)
                .createTime(LocalDateTime.now())
                .status(LikeUnlikeContentTypeEnum.UNLIKE.getCode())
                .creatorId(creator)
                .build();
        Message message = MessageBuilder.withPayload(JsonUtil.toJsonString(likeUnlikeMQDTO))
                .build(); // 创建消息对象
        String topicTag = MQConstant.TOPIC_LIKE_OR_UNLIKE + ":" + MQConstant.TAG_UNLIKE;
        String hashkey = String.valueOf(contentId); // 以内容id作为hashkey，保证同一内容的点赞消息落到同一个队列中，保证顺序
        rocketMQTemplate.asyncSendOrderly(topicTag, message, hashkey, new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【内容取消点赞】内容取消点赞发送MQ成功: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(likeUnlikeMQDTO));
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【内容取消点赞】内容取消点赞发送MQ失败: {}, 消息体: {}", topicTag, JsonUtil.toJsonString(likeUnlikeMQDTO), throwable);
            }
        });
        return Response.success("取消点赞成功");
    }

    @Override
    public Response<?> CollectContent(CollectContentReqVo collectContentReqVo) {
        Long contentId = Long.valueOf(collectContentReqVo.getContentId());
        // 1. 校验被收藏的笔记是否存在
        Long creator = checkContentIsExist(contentId);
        // 2. 判断是否已经收藏过了
        Long userId = LoginUserContextHolder.getUserId();
        log.info("收藏接口接收的上下文id: {}", userId);
        String bloomFilterKey = ContentDetailsKeyConstant.getBloomUserContentCollectListKey(userId);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_content_collect_check.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId);
        ContentCollectTypeEnum contentCollectTypeEnum = ContentCollectTypeEnum.valueOf(result);
        String userContentCollectZsetKey = ContentDetailsKeyConstant.getBloomUserContentCollectZSetKey(userId);
        Long userid;
        userid = userId;
        switch (contentCollectTypeEnum) {
            case NOT_EXIST ->{
                int count = contentCollectionDoMapper.selectCountByUserIdAndContentId(userId, contentId);
                long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                if (count >0) {
                    threadPoolTaskExecutor.execute(()->{batchAddContentCollect2R64BitmapAndExpire(userid, expireTime, bloomFilterKey);});
                    throw new BusinessException(ResponseStatusEnum.CONTENT_COLLECTED);
                };
                // 将当前收藏的内容id添加到布隆过滤器中
                DefaultRedisScript<Long> redisScript2 = new DefaultRedisScript<>();
                redisScript2.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_add_content_collect_and_expire.lua")));
                redisScript2.setResultType(Long.class);
                redisTemplate.execute(redisScript2, Collections.singletonList(bloomFilterKey), contentId, expireTime);
            }
            case COLLECTED ->{
                // 可能误判，查询数据库，如果数据库中有数据，提示已经收藏过了
                /*Double score = redisTemplate.opsForZSet().score(userContentCollectZsetKey, contentId);
                if (Objects.nonNull(score)) {
                    // redis中查询
                    throw new BusinessException(ResponseStatusEnum.CONTENT_COLLECTED);
                }
                int count = contentCollectionDoMapper.selectCountByUserIdAndContentId(userId, contentId);
                if (count > 0) {
                    // 数据库查询，判断此时redis中对应的set集合中对应的值是否存在，并更新集合
                    log.info("===> 代码走到了这里【COLLECTED】");
                    asynInitUserContentCollectsZSet(userid, userContentCollectZsetKey);
                    throw new BusinessException(ResponseStatusEnum.CONTENT_COLLECTED);
                }*/
                throw new BusinessException(ResponseStatusEnum.CONTENT_COLLECTED);
            }
        }
        // 3. 更新redisZSet收藏列表
        LocalDateTime now = LocalDateTime.now();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/content_collect_check_and_update_zset.lua")));
        redisScript.setResultType(Long.class);
        result = redisTemplate.execute(redisScript, Collections.singletonList(userContentCollectZsetKey), contentId, DateUtil.LocalTimestampToDate(now));
        if (Objects.equals(result, NOT_EXIST.getCode())) {
            List<ContentCollectionDo> contentCollectionDos = contentCollectionDoMapper.selectCollectedByUserIdAndLimit(userid, 300);
            DefaultRedisScript<Long> redisScript2 = new DefaultRedisScript<>();
            redisScript2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_content_collect_zset_and_expire.lua")));
            redisScript2.setResultType(Long.class);
            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天
            if (CollUtil.isNotEmpty(contentCollectionDos)) {
                Object[] Args = getArgsForBatchAddContentCollectZSet(contentCollectionDos, expireTime);
                redisTemplate.execute(redisScript2, Collections.singletonList(userContentCollectZsetKey), Args);

                redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId, DateUtil.LocalTimestampToDate(now));
            } else {
                List<Object> luaArgs = Lists.newArrayList();
                // 插入的顺序：时间戳，内容id，过期时间
                luaArgs.add(DateUtil.LocalTimestampToDate(now));
                luaArgs.add(contentId);
                luaArgs.add(expireTime);
                redisTemplate.execute(redisScript2, Collections.singletonList(userContentCollectZsetKey), luaArgs.toArray());
            }
        }
        // 4. 发送 MQ, 将收藏数据落库
        ContentCollectUnCollectMQDTO contentCollectUnCollectMQDTO = ContentCollectUnCollectMQDTO.builder()
                .userId(userid)
                .contentId(contentId)
                .createTime(now)
                .status(CollectUnColletcContentEnum.COLLECT.getStatus())
                .creatorId(creator)
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(contentCollectUnCollectMQDTO)).build();
        String topicTag = MQConstant.TOPIC_COLLECT_OR_UNCOLLECT + ":" + MQConstant.TAG_COLLECT;
        String hashkey = String.valueOf(contentId);
        rocketMQTemplate.asyncSendOrderly(topicTag, message, hashkey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【内容收藏】内容收藏发送MQ成功: {}, 消息体: {}", topicTag, sendResult.getMessageQueue());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【内容收藏】内容收藏发送MQ失败: {}, 消息体: {}", topicTag, throwable.getStackTrace());
            }
        });
        return Response.success("收藏成功");
    }

    private void batchAddContentCollect2R64BitmapAndExpire(Long userid, long expireTime, String bloomFilterKey) {
        List<ContentCollectionDo> contentCollectionDos = contentCollectionDoMapper.selectContentsByUserId(userid);
        List<Object> args = Lists.newArrayList();
        contentCollectionDos.forEach(contentCollectionDo -> {
            args.add(contentCollectionDo.getContentId());
        });
        args.add(expireTime);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_batch_add_content_collect_and_expire.lua")));
        redisScript.setResultType(Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), args.toArray());
    }

    @Override
    public Response<?> UnCollectContent(UnCollectContentReqVo unCollectContentReqVo) {
        Long contentId = Long.valueOf(unCollectContentReqVo.getContentId());
        // 1. 校验被取消收藏的笔记是否存在
        Long creator = checkContentIsExist(contentId);
        // 2. 判断是否已经收藏过了
        Long userId = LoginUserContextHolder.getUserId();
        String bloomFilterKey = ContentDetailsKeyConstant.getBloomUserContentCollectListKey(userId);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_content_uncollect_checked.lua")));
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), contentId);
        ContentUnCollectluaResultEnum contentUnCollectluaResultEnum = ContentUnCollectluaResultEnum.valueOf(result);
        if (Objects.isNull(contentUnCollectluaResultEnum)) throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
        Long userid;
        userid = userId;
        switch (contentUnCollectluaResultEnum) {
            case NOT_EXISTS -> {
                threadPoolTaskExecutor.submit(()->{
                    long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数
                    batchAddContentCollect2R64BitmapAndExpire(userid, expireTime, bloomFilterKey);
                });
                int count = contentCollectionDoMapper.selectCountByUserIdAndContentId(userId, contentId);
                if (count <= 0) throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_COLLECTED);
            }
            case UNCOLLECTED -> throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_COLLECTED);
        }
        String userContentCollectZsetKey = ContentDetailsKeyConstant.getBloomUserContentCollectZSetKey(userId);
        redisTemplate.opsForZSet().remove(userContentCollectZsetKey, contentId); // 更新redisZSet收藏列表
        // 4. 发送 MQ, 将取消收藏数据落库
        ContentCollectUnCollectMQDTO contentCollectUnCollectMQDTO = ContentCollectUnCollectMQDTO.builder()
                .userId(userid)
                .contentId(contentId)
                .createTime(LocalDateTime.now())
                .status(CollectUnColletcContentEnum.UNCOLLECTED.getStatus())
                .creatorId(creator)
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtil.toJsonString(contentCollectUnCollectMQDTO)).build();
        String topicTag = MQConstant.TOPIC_COLLECT_OR_UNCOLLECT + ":" + MQConstant.TAG_UNCOLLECT;
        String hashkey = String.valueOf(contentId);
        rocketMQTemplate.asyncSendOrderly(topicTag, message, hashkey, new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("【内容取消收藏】内容取消收藏发送MQ成功: {}, 消息体: {}", topicTag, sendResult.getMessageQueue());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("【内容取消收藏】内容取消收藏发送MQ失败: {}, 消息体: {}", topicTag, throwable.getStackTrace());
            }
        });
        return Response.success("取消收藏成功");
    }

    /**
     * 异步批量添加内容收藏到布隆过滤器中
     * @param userId
     * @param expireTime
     * @param bloomFilterKey
     */
    private void asyncBatchAddContentCollectToBloomFilter(Long userId, long expireTime, String bloomFilterKey) {
        try {
            List<ContentCollectionDo> contentCollectionDos = contentCollectionDoMapper.selectContentsByUserId(userId);
            log.info("===> <UNK>{}",contentCollectionDos);
            if (CollUtil.isNotEmpty(contentCollectionDos)) {
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_content_collect_and_expire.lua")));
                redisScript.setResultType(Long.class);
                List<Object> luaArgs = Lists.newArrayList();
                contentCollectionDos.forEach(contentCollectionDo -> {
                    luaArgs.add(contentCollectionDo.getContentId());
                });
                // 在最后一个参数添加过期时间
                luaArgs.add(expireTime);
                redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记收藏】布隆过滤器异常: ", e);
        }
    }

    private void asynInitUserContentCollectsZSet(Long userId, String userContentCollectedZSetKey) {
        threadPoolTaskExecutor.submit(()->{
           boolean isZSetExist = redisTemplate.hasKey(userContentCollectedZSetKey);
              if(Objects.equals(Boolean.FALSE,isZSetExist)) { // 不存在，初始化ZSet
                  List<ContentCollectionDo> contentCollectionDos = contentCollectionDoMapper.selectCollectedByUserIdAndLimit(userId, 300);
                    if (CollUtil.isNotEmpty(contentCollectionDos)) {
                        long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数
                        Object[] luaArgs = getArgsForBatchAddContentCollectZSet(contentCollectionDos, expireTime);
                        DefaultRedisScript<Long> redisScriptZSet = new DefaultRedisScript<>();
                        redisScriptZSet.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_content_collect_zset_and_expire.lua")));
                        redisScriptZSet.setResultType(Long.class);

                        redisTemplate.execute(redisScriptZSet, Collections.singletonList(userContentCollectedZSetKey), luaArgs);
                    }
              }
        });
    }

    private Object[] getArgsForBatchAddContentCollectZSet(List<ContentCollectionDo> contentCollectionDos, long expireTime) {
        int argsLen = contentCollectionDos.size() * 2 + 1; // 参数长度：内容id和时间戳各一个 + 过期时间一个
        Object[] luaArgs = new Object[argsLen];
        int i = 0;
        for (ContentCollectionDo contentCollectionDo : contentCollectionDos) {
            // 结构: [score, contentId, score, contentId, ..., expireTime] 上面关注方法中也是这个结构存在ZSet里
            luaArgs[i] = DateUtil.LocalTimestampToDate(contentCollectionDo.getCreateTime());
            luaArgs[i + 1] = contentCollectionDo.getContentId();
            i += 2;
        }
        // 还差一个过期时间参数
        luaArgs[argsLen - 1] = expireTime;
        return luaArgs;
    }

    /**
     * 异步初始化ZSet，防止频繁调用数据库查询
     * @param userid 当前用户id
     * @param userContentLikedZsetKey 用户点赞内容的ZSet的redis key
     */
    private void asyncInitContentLikedZSet(Long userid, String userContentLikedZsetKey) {
        // 异步初始化ZSet，防止频繁调用数据库查询
        threadPoolTaskExecutor.submit(()->{
            // 查询redis中的Zset是否存在，存在就不初始化了，避免重复初始化
            boolean isZSetExist = redisTemplate.hasKey(userContentLikedZsetKey);
            if(!isZSetExist) { // 不存在，初始化ZSet
                // 查询数据库的100条点赞数据，初始化ZSet
                List<ContentLikeDo> contentLikeDoList = contentLikeDoMapper.selectLikedByUserIdAndLimit(userid,100);
                if(CollUtil.isNotEmpty(contentLikeDoList)) {
                    long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数
                    Object[] luaArgs = getArgsForBatchAddContentLikeZSet(contentLikeDoList, expireTime);
                    DefaultRedisScript<Long> redisScriptZSet = new DefaultRedisScript<>();
                    redisScriptZSet.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_content_like_zset_and_expire.lua")));
                    redisScriptZSet.setResultType(Long.class);

                    redisTemplate.execute(redisScriptZSet, Collections.singletonList(userContentLikedZsetKey), luaArgs);
                }
            }
        });
    }

    /**
     * 获取批量添加内容点赞到ZSet的Lua脚本参数
     * @param contentLikeDoList 点赞数据列表
     * @param expireTime 过期时间
     * @return Lua脚本参数数组
     */
    private Object[] getArgsForBatchAddContentLikeZSet(List<ContentLikeDo> contentLikeDoList, long expireTime) {
        int argsLen = contentLikeDoList.size() * 2 + 1; // 参数长度：内容id和时间戳各一个 + 过期时间一个
        Object[] luaArgs = new Object[argsLen];

        int i = 0;
        for (ContentLikeDo contentLikeDo : contentLikeDoList) {
            // 结构: [score, contentId, score, contentId, ..., expireTime] 上面关注方法中也是这个结构存在ZSet里
            luaArgs[i] = DateUtil.LocalTimestampToDate(contentLikeDo.getCreateTime());
            luaArgs[i + 1] = contentLikeDo.getContentId();
            i += 2;
        }
        // 还差一个过期时间参数
        luaArgs[argsLen - 1] = expireTime;
        return luaArgs;
    }

    /**
     * 异步批量添加内容点赞到布隆过滤器中
     * @param userid 当前用户id
     * @param expireTime 过期时间
     * @param bloomFilterKey 布隆过滤器的redis key
     */
    private void asyncBatchAddContentLikeToBloomFilter(Long userid, long expireTime, String bloomFilterKey) {
        threadPoolTaskExecutor.submit(()->{
            try {
                // 查询该用户所有点赞的内容id
                List<ContentLikeDo> contentDoList = contentLikeDoMapper.selectContentsIdByUserId(userid);
                if (CollUtil.isNotEmpty(contentDoList)) {
                    // 查到了，批量添加到布隆过滤器中
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_content_like_and_expire.lua")));
                    redisScript.setResultType(Long.class);
                    List<Object> luaArgs = Lists.newArrayList();
                    contentDoList.forEach(contentLikeDo -> {
                        luaArgs.add(contentLikeDo.getContentId());
                    });
                    // 在最后一个参数添加过期时间
                    luaArgs.add(expireTime);
                    redisTemplate.execute(redisScript, Collections.singletonList(bloomFilterKey), luaArgs.toArray());
                }
            } catch (Exception e) {
                log.error("异步批量添加内容点赞到布隆过滤器失败，用户id：{}", userid, e);
            }
        });
    }

    /**
     * 内容存在性校验
     * @param contentId 内容id
     */
    private Long checkContentIsExist(Long contentId) {
        // 现在本地缓存中查找 getIfPresent方法：返回value / null
        String findContentDetailForLocalCache = LOCAL_CACHE.getIfPresent(contentId);
        ContentDetailsResVo contentDetailsResVo = JsonUtil.parseObject(findContentDetailForLocalCache,ContentDetailsResVo.class);
//        log.info("===> 【内容服务】内容存在性校验:{}",contentDetailsResVo);
        if (Objects.isNull(contentDetailsResVo)) {
            // 在redis中查找
            String contentRedisKey = ContentDetailsKeyConstant.getContentDetailsKey(contentId);
            String contentDetailValue = redisTemplate.opsForValue().get(contentRedisKey);
            // 将redis的数据转为相应对象
            contentDetailsResVo = JsonUtil.parseObject(contentDetailValue, ContentDetailsResVo.class);
            if(Objects.isNull(contentDetailsResVo)) {
                // 在数据库中查找
                Long creator = contentDoMapper.selectCreatorByContentId(contentId);
                if (Objects.isNull(creator)) { // 数据库中也没有，说明内容确实不存在
                    throw new BusinessException(ResponseStatusEnum.CONTENT_NOT_EXIST);
                }
                // 存在，同步缓存
                threadPoolTaskExecutor.submit(()->{
                    ContentDetailsReqVo contentDetailsReqVo = ContentDetailsReqVo.builder().Id(String.valueOf(contentId)).build();
                    // 直接调用查找方法
                    ContentDetails(contentDetailsReqVo);
                });
                return creator;
            }
        }
        return Long.valueOf(contentDetailsResVo.getCreatorId());
    }

    /**
     * 内容的可见性校验
     * @param visible 可见性字段
     * @param creatorId 发布者id
     * @param UserId 当前id
     * @return 判断
     */
    private boolean checkContentVisible(Integer visible, Long creatorId, Long UserId) {
        return !Objects.equals(visible, ContentVisibleEnum.PRIVATE.getCode()) || Objects.equals(creatorId, UserId);
    }

    @Override
    public Response<LikeCollectStatusJudgeResVo> LikeCollectStatusJudge(LikeCollectStatusJudge likeCollectStatusJudgeReqVo) {
        Long contentId = Long.valueOf(likeCollectStatusJudgeReqVo.getContentId());
        Long userId = LoginUserContextHolder.getUserId();

        boolean like = false;
        boolean collect = false;

        if (Objects.isNull(userId)) throw new BusinessException(ResponseStatusEnum.PARAMS_NOT_VALID.getErrorCode()
        ,"未登录/失效");
        String userContentLikedZsetKey = ContentDetailsKeyConstant.getBloomUserContentLikeListKey(userId);
        String userContentCollectedZsetKey = ContentDetailsKeyConstant.getBloomUserContentCollectListKey(userId);

        like = isLiked(userId, contentId, userContentLikedZsetKey);
        collect = isCollected(userId, contentId, userContentCollectedZsetKey);



        LikeCollectStatusJudgeResVo likeCollectStatusJudgeResVo = LikeCollectStatusJudgeResVo.builder()
                .contentId(String.valueOf(contentId))
                .like(like)
                .collect(collect)
                .build();
        return Response.success(likeCollectStatusJudgeResVo);
    }

    private boolean isCollected(Long userId, Long contentId, String userContentCollectedZsetKey) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_content_colloect_only_check.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(userContentCollectedZsetKey), contentId);
        ContentCollectTypeEnum contentCollectTypeEnum = ContentCollectTypeEnum.valueOf(result);
        if (Objects.nonNull(contentCollectTypeEnum)) {
            switch (contentCollectTypeEnum) {
                case NOT_EXIST -> {
                    int count = contentCollectionDoMapper.selectCountByUserIdAndContentId(userId, contentId);
                    long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    if (count >0) {
                        threadPoolTaskExecutor.execute(()->{batchAddContentCollect2R64BitmapAndExpire(userId, expireTime, userContentCollectedZsetKey);});
                        return true;
                    };
                }
                case COLLECTED -> {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLiked(Long userId, Long contentId, String userContentLikedZsetKey) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_bitmap_content_like_only_check.lua")));
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(userContentLikedZsetKey), contentId);
        ContentCollectTypeEnum contentCollectTypeEnum = ContentCollectTypeEnum.valueOf(result);
        if (Objects.nonNull(contentCollectTypeEnum)) {
            switch (contentCollectTypeEnum) {
                case COLLECTED -> {
                    return true;
                }
                case NOT_EXIST -> {
                    int count = contentLikeDoMapper.selectCountByUserIdAndContentId(userId, contentId);

                    long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24); // 随机过期时间，1天+一天内的随机数

                    if (count > 0) {
                        // 表示点赞过 数据库查到了就要更新布隆过滤器
                        // 异步初始化 Roaring Bitmap
                        threadPoolTaskExecutor.submit(() ->
                                batchAddContentLike2R64BitmapAndExpire(userId,expireTime,userContentLikedZsetKey));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Response<ContentPublishListResVo> ContentPublishList(ContentPublishListReqVo contentPublishListReqVo) {
        Long userId = Long.valueOf(contentPublishListReqVo.getUserId());

        Long cursor = contentPublishListReqVo.getCursor();
        ContentPublishListResVo contentPublishListResVo = null;
        // todo 先缓存
        String contentPublishListKey = ContentDetailsKeyConstant.getUserPublishContentListKey(userId);
        if (Objects.isNull(cursor)) {
            String contentPublishListKeyValue = redisTemplate.opsForValue().get(contentPublishListKey);
            if (StringUtils.isNotBlank(contentPublishListKeyValue)) {
                try {
                    List<ContentPublishListItemResVo> contentPublishListItemResVos = JsonUtil.parseList(contentPublishListKeyValue, ContentPublishListItemResVo.class);
                    List<ContentPublishListItemResVo> sortedList = contentPublishListItemResVos.stream()
                            .sorted(Comparator.comparing(ContentPublishListItemResVo::getCreateTime).reversed()).toList();
                    // 获取最后一个id为游标
                    Optional<Long> lastId = sortedList.stream().map(ContentPublishListItemResVo::getContentId).min(Long::compareTo);
                    // 实时更新作者的点赞数
                    getAndSetLastLikedTotalForCreator(userId,sortedList);
                    // 获取笔记的点赞状态
                    batchGetLikedContent(sortedList);
                    contentPublishListResVo = ContentPublishListResVo.builder()
                            .contents(sortedList)
                            .nextCursor(lastId.orElse(null))
                            .build();
                    return Response.success(contentPublishListResVo);
                } catch (Exception e) {
                    log.error("====【用户发布列表】===> 数据解析错误",e);
                }
            }
        }
        // todo 数据库
        List<ContentDo> contentDos = contentDoMapper.selectPublishContentByUserIdAndCursor(userId,cursor);
        if (CollUtil.isNotEmpty(contentDos)) {
            List<ContentPublishListItemResVo> contentPublishListItemResVos = contentDos.stream()
                    .map(contentDo -> {
                        Integer type = contentDo.getType();
                        // 图片
                        String imgUris = contentDo.getImgUris();
                        List<String> imgUris_list = null;
                        if (Objects.equals(type,ContentTypeEnum.IMAGE.getCode()) && StringUtils.isNotBlank(imgUris)) {
                            imgUris_list = Arrays.asList(imgUris.split(","));
                        }
                        // 视频
                        String videoUris = contentDo.getVideoUri();
                        List<String> videoUris_list = null;
                        if (Objects.equals(type,ContentTypeEnum.VIDEO.getCode()) && StringUtils.isNotBlank(videoUris)) {
                            videoUris_list = Arrays.asList(videoUris.split(","));
                        }
                        // 文件
                        String fileUris = contentDo.getFileUris();
                        List<String> fileUris_list = null;
                        if (Objects.equals(type,ContentTypeEnum.FILE.getCode()) && StringUtils.isNotBlank(fileUris)) {
                            fileUris_list = Arrays.asList(fileUris.split(","));
                        }
                        // 链接
                        String linkUris = contentDo.getUrlUris();
                        List<String> linkUris_list = null;
                        if (Objects.equals(type,ContentTypeEnum.LINK.getCode()) && StringUtils.isNotBlank(linkUris)) {
                            linkUris_list = Arrays.asList(linkUris.split(","));
                        }
                        return ContentPublishListItemResVo.builder()
                                .contentId(contentDo.getId())
                                .cover(Objects.isNull(contentDo.getImgUris())?"":contentDo.getImgUris())
                                .title(contentDo.getTitle())
                                .type(type)
                                .imgUris(imgUris_list)
                                .videoUris(videoUris_list)
                                .fileUris(fileUris_list)
                                .linkUris(linkUris_list)
                                .creatorId(contentDo.getCreatorId())
                                .isTop(contentDo.getIsTop())
                                .createTime(contentDo.getCreateTime())
                                .isLiked(false) // 默认未点赞
                                .build();
                    })
                    .toList();

            // todo 调用远程服务
            /*
            CompletableFuture.supplyAsync() 创建一个异步任务，并返回一个CompletableFuture对象。
             */
            CompletableFuture<FindUserByIdResDTO> findUserByIdResDTOFuture = CompletableFuture.supplyAsync(()->{
                Optional<Long> creatorIdOptional = contentDos.stream().map(ContentDo::getCreatorId).findAny();
                return userFeignApiService.getUserInfoById(creatorIdOptional.get());
            },threadPoolTaskExecutor);
            CompletableFuture<List<FindContentCountResDTO>> findContentCountResDTOSFuture = CompletableFuture.supplyAsync(()->{
                List<Long> contentIds = contentDos.stream().map(ContentDo::getId).toList();
                return countFeignServer.findContentCount(contentIds);
            },threadPoolTaskExecutor);
            CompletableFuture.allOf(findUserByIdResDTOFuture, findContentCountResDTOSFuture);
            try {
                FindUserByIdResDTO findUserByIdResDTO = findUserByIdResDTOFuture.get();
                List<FindContentCountResDTO> contentCountResDTOS = findContentCountResDTOSFuture.get();
                if (CollUtil.isNotEmpty(contentCountResDTOS)) {
                    contentPublishListItemResVos.forEach(contentPublishListItemResVo -> {
                        contentPublishListItemResVo.setCreatorName(findUserByIdResDTO.getNickname());
                        contentPublishListItemResVo.setAvatar(findUserByIdResDTO.getAvatar());
                    });
                }
                if (CollUtil.isNotEmpty(contentCountResDTOS)) {
                    Map<Long,FindContentCountResDTO> findContentCountResDTOMap = contentCountResDTOS.stream()
                            .collect(Collectors.toMap(FindContentCountResDTO::getContentId, findContentCountResDTO -> findContentCountResDTO));

                    contentPublishListItemResVos.forEach(contentPublishListItemResVo -> {
                        Long contentId = contentPublishListItemResVo.getContentId();
                        FindContentCountResDTO findContentCountResDTO = findContentCountResDTOMap.get(contentId);
                        contentPublishListItemResVo.setLikeTotal(Objects.isNull(findContentCountResDTO.getLikeTotal())? "0": String.valueOf(findContentCountResDTO.getLikeTotal()));
                    });
                }
                // 批量设置点赞总数redis
                setVOListLikeTotal(contentPublishListItemResVos, contentCountResDTOS);

                // 批量获取点赞状态
                batchGetLikedContent(contentPublishListItemResVos);
            } catch (Exception e) {
                log.error("【发布列表查询】并发调用错误",e);
            }
            Optional<Long> lastContentIdOptional = contentDos.stream().map(ContentDo::getId).min(Long::compareTo);
            contentPublishListResVo = ContentPublishListResVo.builder()
                    .contents(contentPublishListItemResVos)
                    .nextCursor(lastContentIdOptional.get())
                    .build();

            // 存第一页发布内容
            if (Objects.isNull(cursor)) {
                syncFirstPagePublished2Redis(contentPublishListResVo, contentPublishListKey);
            }

        }

        // todo 缓存
        return Response.success(contentPublishListResVo);
    }

    private void batchGetLikedContent(List<ContentPublishListItemResVo> sortedList) {
        Long userId = LoginUserContextHolder.getUserId();
        if (Objects.nonNull(userId)) {
            List<Long> contentIds = sortedList.stream().map(ContentPublishListItemResVo::getContentId).toList();
            String userContentLikedKey = ContentDetailsKeyConstant.getBloomUserContentLikeListKey(userId);

            DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/r64_batch_get_like_content.lua")));
            redisScript.setResultType(List.class);

            List<Long> results = redisTemplate.execute(redisScript, Lists.newArrayList(userContentLikedKey), contentIds.toArray());
            Long result = results.get(0);
            if (Objects.equals(result, ContentLikeLuaResultEnum.NOT_EXIST.getCode())) {
                List<ContentLikeDo> contentLikeDos = contentLikeDoMapper.selectByContentIdAndContentIds(userId, contentIds);
                if (CollUtil.isNotEmpty(contentLikeDos)) {
                    Map<Long, ContentLikeDo> contentLikeDoMap = contentLikeDos.stream()
                            .collect(Collectors.toMap(ContentLikeDo::getContentId, contentLikeDo -> contentLikeDo));
                    for (ContentPublishListItemResVo contentPublishListItemResVo : sortedList) {
                        Long contentId = contentPublishListItemResVo.getContentId();
                        ContentLikeDo contentLikeDo = contentLikeDoMap.get(contentId);
                        contentPublishListItemResVo.setLiked(Objects.nonNull(contentLikeDo));
                    }
                    threadPoolTaskExecutor.execute(() -> {
                        asyncBatchAddContentLikeToBloomFilter(userId, 60 * 30 + RandomUtil.randomInt(60*30), userContentLikedKey);
                    });
                    return;
                }
            }
            // 存在，获取状态
            Map<Long,Boolean> liekdMap = new HashMap<>(results.size());
            for (int i = 0; i < results.size(); i++) {
                Boolean status = Objects.equals(results.get(i), ContentLikeLuaResultEnum.BLOOM_LIKED.getCode());
                Long contentId = contentIds.get(i);
                liekdMap.put(contentId, status);
            }
            sortedList.forEach(contentPublishListItemResVo -> {
                contentPublishListItemResVo.setLiked(liekdMap.get(contentPublishListItemResVo.getContentId()));
            });
        }
    }

    private void setVOListLikeTotal(List<ContentPublishListItemResVo> contentPublishListItemResVos, List<FindContentCountResDTO> contentCountResDTOS) {
        if (CollUtil.isNotEmpty(contentCountResDTOS)) {
            // DTO 集合转 Map
            Map<Long, FindContentCountResDTO> contentIdAndDTOMap = contentCountResDTOS.stream()
                    .collect(Collectors.toMap(FindContentCountResDTO::getContentId, dto -> dto));

            // 循环设置 VO 集合，设置每篇笔记的点赞量
            contentPublishListItemResVos.forEach(noteItemRspVO -> {
                Long currNoteId = noteItemRspVO.getContentId();
                FindContentCountResDTO findContentCountsByIdRspDTO = contentIdAndDTOMap.get(currNoteId);
                noteItemRspVO.setLikeTotal((Objects.nonNull(findContentCountsByIdRspDTO) && Objects.nonNull(findContentCountsByIdRspDTO.getLikeTotal())) ?
                        NumberUtil.formatNumberString(findContentCountsByIdRspDTO.getLikeTotal()) : "0");
            });
        }
    }

    private void getAndSetLastLikedTotalForCreator(Long userId, List<ContentPublishListItemResVo> sortedList) {
        Long momentUserId = LoginUserContextHolder.getUserId();
        if (Objects.equals(userId, momentUserId)) {
            // 获取用户点赞数
            CompletableFuture<List<FindContentCountResDTO>> findContentCountResDTOSFuture = CompletableFuture.supplyAsync(()->{
                List<Long> contentIds = sortedList.stream().map(ContentPublishListItemResVo::getCreatorId).toList();
                return countFeignServer.findContentCount(contentIds);
            },threadPoolTaskExecutor);
            try {
                /*FindUserByIdResDTO findUserByIdResDTO = findUserByIdResDTOFuture.get(); // 更新用户的数据消息*/
                List<FindContentCountResDTO> contentCountResDTOS = findContentCountResDTOSFuture.get();
                setVOListLikeTotal(sortedList, contentCountResDTOS);
            } catch (Exception e) {
                log.error("【发布列表查询(作者)】并发调用错误",e);
            }
        }

    }

    private void syncFirstPagePublished2Redis(ContentPublishListResVo contentPublishListResVo, String contentPublishListKey) {
        if (CollUtil.isEmpty(contentPublishListResVo.getContents())) return;
        threadPoolTaskExecutor.execute(() -> {
            long expireTime = 60 * 10 + RandomUtil.randomInt(60*30); // 随机10-30分钟
            redisTemplate.opsForValue().set(contentPublishListKey, JsonUtil.toJsonString(contentPublishListResVo.getContents()), expireTime, TimeUnit.SECONDS);
        });
    }
}

