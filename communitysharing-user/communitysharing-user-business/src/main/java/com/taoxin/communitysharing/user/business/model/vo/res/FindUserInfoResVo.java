package com.taoxin.communitysharing.user.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserInfoResVo {

    private Long userId;

    private String nickname;

    private String avatar;

    private String backgroundImg;

    private String communitysharingId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 账号年龄
     */
    private Integer years;

    private Integer sex;

    private String introduction;

    private String fansTotal;

    private String followingTotal;

    private String contentTotal;

    private String likeTotal;

    private String collectTotal;
}
