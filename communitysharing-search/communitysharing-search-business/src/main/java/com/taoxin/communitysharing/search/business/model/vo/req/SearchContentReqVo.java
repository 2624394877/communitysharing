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
public class SearchContentReqVo {
    @NotBlank(message = "关键字不能为空")
    private String keyword;
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    // 搜索类型：null 为所有，1 为内容，2 为用户
    private Integer type;
    // 排序方式：null 为默认，1 为更新时间降序，2 为点赞数降序，3 为评论数降序，4 为收藏数降序
    private Integer sort;
    // 时间范围：null 为所有，0 为最近一天，2 为最近一个周，3 为最近一个月，4 为最近半年
    private Integer timeRange;
}
