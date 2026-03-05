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
public class ContentPublishListItemResVo {

    private Long contentId;

    private Integer type;

    private String cover;

    private List<String> videoUris;

    private List<String> imgUris;

    private List<String> fileUris;

    private List<String> linkUris;

    private LocalDateTime createTime;

    private boolean isTop;

    private String title;

    private Long creatorId;

    private String creatorName;

    private String avatar;

    private String likeTotal;

    private boolean isLiked;
}
