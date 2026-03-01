package com.taoxin.communitysharing.KV.domian.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("comment_content")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentContentDO {
    @PrimaryKey
    private CommentContentPrimaryKey commentContentPrimaryKey;

    private String comment; // 评论内容
}
