package com.taoxin.communitysharing.search.business.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchUserResVo {
    private Long userId; // 用户id
    private String communitySharingId; // 社区分享id
    private String nickname; // 昵称
    private String avatar; // 头像
    private String contentTotal; // 内容总数
    private String fansTotal; // 粉丝总数
    private String HighLightKeyword;
    private String likeTotal;
}
