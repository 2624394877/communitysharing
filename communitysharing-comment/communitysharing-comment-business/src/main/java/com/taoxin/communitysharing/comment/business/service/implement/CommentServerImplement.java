package com.taoxin.communitysharing.comment.business.service.implement;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.KV.dto.request.FindCommentContentReqDTO;
import com.taoxin.communitysharing.KV.dto.response.FindCommentContentRspDTO;
import com.taoxin.communitysharing.comment.business.constant.MQConstant;
import com.taoxin.communitysharing.comment.business.domain.databaseObject.CommentDo;
import com.taoxin.communitysharing.comment.business.domain.mapper.CommentDoMapper;
import com.taoxin.communitysharing.comment.business.domain.mapper.ContentCountDoMapper;
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
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        // todo 数据库查询
        // 查询评论总数
        Long totalCount = contentCountDoMapper.selectCountCommentByContentId(contentId);
        if (Objects.isNull(totalCount)) return PageResponse.success(null,pageNo,0L,pageSize);
        // 分页返参
        List<FindCommentItemRspVo> findCommentItemRspVoList = null;
        if (totalCount > 0) {
            findCommentItemRspVoList = Lists.newArrayList();
            // 计算offset
            long offset = PageResponse.getOffset(pageNo, pageSize);
            // 查询一级评论
            List<CommentDo> commentDoList = commentDoMapper.selectPageList(contentId, offset, pageSize);
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
                log.info("commentDo: {}", commentDo);
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
                        .childrenCommentTotal(commentDo.getChildCommentTotal()) // 子评论数
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
                                .build();
                        setCommentcontent(firstReplyCommentItemRspVo, firstReplyCommentUser, commentContentMap);
                        setUserInfo(userInfoMap,firstReplyUserId,firstReplyCommentItemRspVo);
                        // 将二级评论数据设置到一级评论中
                        firstCommentItemRspVo.setFirstReplyComment(firstReplyCommentItemRspVo);
                    }
                }
                log.info("一级评论拼接：{}",firstCommentItemRspVo);
                findCommentItemRspVoList.add(firstCommentItemRspVo);
            }

        }
        return PageResponse.success(findCommentItemRspVoList,pageNo,totalCount,pageSize);
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
