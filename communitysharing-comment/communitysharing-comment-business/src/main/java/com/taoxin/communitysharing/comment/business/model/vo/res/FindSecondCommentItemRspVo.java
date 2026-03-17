package com.taoxin.communitysharing.comment.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindSecondCommentItemRspVo {
    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 评论内容
     */
    private String comment;

    /**
     * 评论的图片地址
     */
    private String imageUrl;

    /**
     * 评论发布时间
     */
    private String createTime;

    /**
     * 评论点赞数
     */
    private Long likeTotal;

    /**
     * 回复的用户昵称
     */
    private String replyUserName;

    /**
     * 回复的用户 ID
     */
    private Long replyUserId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否点赞
     */
    private boolean like;
}
