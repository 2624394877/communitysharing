package com.taoxin.communitysharing.KV.domian.repository;

import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentDO;
import com.taoxin.communitysharing.KV.domian.dataobject.CommentContentPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommentContentRepository extends CassandraRepository<CommentContentDO, CommentContentPrimaryKey> {
    /**
     * 根据内容ID、月份、评论ID查询笔记内容
     * @param contentId 内容id
     * @param yearMonths 月份
     * @param commentIds 评论id
     * @return 评论表内容列表
     */
    @Query("select * from comment_content where content_id = ?0 and year_month in ?1 and comment_id in ?2")
    List<CommentContentDO> findComment(Long contentId, List<String> yearMonths, List<UUID> commentIds);
}
