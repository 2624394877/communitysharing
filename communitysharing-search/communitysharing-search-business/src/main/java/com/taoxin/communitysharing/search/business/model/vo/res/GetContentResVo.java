package com.taoxin.communitysharing.search.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetContentResVo {
    private Long contentId;

    private String cover;

    private String title;

    private String avatar;

    private String nickname;

    private String topic;

    private Integer type;

    private String likeTotal;

    private String collectTotal;

    private String commentTotal;

    private String createTime;

    private String updateTime;
}
