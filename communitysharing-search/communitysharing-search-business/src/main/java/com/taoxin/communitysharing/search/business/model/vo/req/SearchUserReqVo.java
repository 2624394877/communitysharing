package com.taoxin.communitysharing.search.business.model.vo.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchUserReqVo {
    @NotBlank(message = "关键字不能为空")
    private String keyword;
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1; // 默认第一页
}
