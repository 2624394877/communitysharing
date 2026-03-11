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
public class ContentUpdateReqVo {
    @NotNull(message = "内容id不能为空")
    private String contentId;

    private String title;

    private String content;

    @NotNull(message = "内容类型不能为空")
    private Integer type;

    private Long topicId;

    private List<String> imgUris;

    private List<String> videoUris;

    private List<String> fileUris;

    private List<String> linkUris;

    @NotNull(message = "内容可见性不能为空")
    private Boolean visible;
}
