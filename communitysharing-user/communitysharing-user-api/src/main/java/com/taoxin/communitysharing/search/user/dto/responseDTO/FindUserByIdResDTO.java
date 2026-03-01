package com.taoxin.communitysharing.search.user.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByIdResDTO {
    private Long id;

    private String communitysharingId;

    private String nickname;

    private String avatar;

    private String backgroundImg;

    private String introduction;

    private boolean isDeleted;
}
