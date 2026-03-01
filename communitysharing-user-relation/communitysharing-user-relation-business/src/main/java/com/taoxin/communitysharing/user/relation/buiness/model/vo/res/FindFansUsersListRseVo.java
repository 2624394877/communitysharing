package com.taoxin.communitysharing.user.relation.buiness.model.vo.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFansUsersListRseVo {
    private Long userId;

    private String communitysharingId;

    private String avatar;

    private String nickname;

    private String introduction;
}
