package com.taoxin.communitysharing.content.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentDetailsResVo {
    private Long id;

    private String title;

    private String content;

    private String creatorId; // 创建者id: 平台的id而不是数据库主键

    private String creatorName;

    private String avatar;

    private Integer type;

    private Long topicId;

    private String topicName;

    private List<String> imgUris;

    private List<String> videoUris;

    private List<String> fileUris;

    private List<String> linkUris;

    private LocalDateTime updateTime;

    private boolean visible; // 是否可见
}
