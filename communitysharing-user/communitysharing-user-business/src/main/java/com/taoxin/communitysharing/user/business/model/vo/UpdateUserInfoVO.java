package com.taoxin.communitysharing.user.business.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserInfoVO {
    /**
     * 用户头像
     */
    private MultipartFile avatar;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户communitysharingId
     */
    private String communitysharingId;
    /**
     * 用户背景图片
     */
    private MultipartFile backgroundImg;
    /**
     * 用户性别
     */
    private Integer sex;
    /**
     * 用户生日
     */
    private LocalDate birthday;
    /**
     * 用户简介
     */
    private String introduction;
}
