package com.taoxin.communitysharing.search.user.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUsersByIdDTO {
    @NotNull(message = "用户id不能为空")
    @Size(min = 1, max = 10, message = "id列表大小须在1-10范围")
    private List<Long> usersId;
}
