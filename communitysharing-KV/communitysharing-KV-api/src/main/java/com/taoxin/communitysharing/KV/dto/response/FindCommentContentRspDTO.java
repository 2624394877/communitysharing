package com.taoxin.communitysharing.KV.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentContentRspDTO {
    /**
     * 评论内容 UUID
     */
    private String commentId;


    /**
     * 评论内容
     */
    private String comment;
}
