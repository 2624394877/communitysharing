package com.taoxin.communitysharing.comment.business.constant;

public class CommentContentKeyConstant {
    /**
     * 回复的评论id
     */
    private static final String ST1_REPLY_COMMENT_ID = "comment:replyId:st1:";

    /**
     * 评论列表
     */
    private static final String CONTENT_COMMENT_LIST_ID = "content:comment:list:";

    /**
     * 评论详情
     */
    private static final String CONTENT_COMMENT_DETAIL = "content:comment:detail:";

    public static String getReplyCommentId(Long commentId) {
        return ST1_REPLY_COMMENT_ID + commentId;
    }

    public static String getCommentListId(Long contentId) {
        return CONTENT_COMMENT_LIST_ID + contentId;
    }

    /**
     * 获取评论详情
     *
     * @param commentId 评论id
     * @return
     */
    public static String getCommentDetail(Object commentId) {
        return CONTENT_COMMENT_DETAIL + commentId;
    }
}
