package com.taoxin.communitysharing.content.business.model.vo.req;

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
public class ContentPublishListReqVo {

    @NotNull(message = "用户ID不能为空")
    private String userId;

    /**
     * 游标
     */
    private Long cursor;

    /*private Integer type;

    private String cover;

    private List<String> videoUris;

    private String title;*/
}
