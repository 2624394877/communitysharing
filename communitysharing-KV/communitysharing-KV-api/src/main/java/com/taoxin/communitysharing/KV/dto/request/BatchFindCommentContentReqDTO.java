package com.taoxin.communitysharing.KV.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchFindCommentContentReqDTO {
    /**
     * 内容id
     */
    @NotNull(message = "内容id不能为空")
    private Long contentId;

    /**
     * 分区键列表
     */
    @NotNull(message = "分区键列表不能为空")
    @Valid // 该注解表示验证对象内部的属性
    List<FindCommentContentReqDTO> commentContentKeys;
}
