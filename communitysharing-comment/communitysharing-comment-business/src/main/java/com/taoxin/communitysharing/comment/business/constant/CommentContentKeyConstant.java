package com.taoxin.communitysharing.comment.business.constant;

public class CommentContentKeyConstant {
    private static final String ST1_REPLY_COMMENT_ID = "comment:replyId:st1:";

    public static String getReplyCommentId(Long commentId) {
        return ST1_REPLY_COMMENT_ID + commentId;
    }
}
