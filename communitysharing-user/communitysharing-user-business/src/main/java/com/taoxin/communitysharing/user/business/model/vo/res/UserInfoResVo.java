package com.taoxin.communitysharing.user.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResVo {
    private String communitysharingId;

    private String nickname;

    private String avatar;

    private LocalDate birthday;

    private String backgroundImg;

    private String email;

    private String phone;

    private Integer sex;

    private String introduction;

    private LocalDateTime createTime;

    private boolean isDeleted;
}
