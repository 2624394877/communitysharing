package com.taoxin.communitysharing.content.business.domain.databaseObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentDo {
    private Long id; // 内容的id

    private String title; // 标题

    private Boolean isContentEmpty; // 内容是否为空

    private Long creatorId; // 创建者id 即 用户id

    private Long topicId; // 话题id

    private String topicName; // 话题名称

    private Boolean isTop; // 是否置顶

    private Integer type; // 内容类型

    private String imgUris; // 图片列表

    private String videoUri; // 视频列表

    private String fileUris; // 文件列表

    private String urlUris; // 链接列表

    private Integer visible; // 是否可见

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间

    private Integer status; // 状态

    private String contentUuid; // 内容在非关系数据库的id
}