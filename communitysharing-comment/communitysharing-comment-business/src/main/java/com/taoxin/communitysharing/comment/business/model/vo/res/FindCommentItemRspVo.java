package com.taoxin.communitysharing.comment.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentItemRspVo {
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
     * 子评论数
     */
    private Long childrenCommentTotal;

    /**
     * 评论的第一个回复
     */
    private FindCommentItemRspVo firstReplyComment;
}
