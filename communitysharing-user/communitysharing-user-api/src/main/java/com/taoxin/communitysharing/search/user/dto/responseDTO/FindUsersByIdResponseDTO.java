package com.taoxin.communitysharing.search.user.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUsersByIdResponseDTO {
    private List<FindUsersByIdResDTO> usersInfo;
}
