package com.taoxin.communitysharing.count.model.dto.Req;

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
public class FindContentCountReqDTO {

    @NotNull(message = "contentId 不能为空")
    @Size(min = 1,max=20, message = "id集合长度1-20")
    private List<Long> contentId;
}
