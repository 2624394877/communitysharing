package com.taoxin.communitysharing.content.business.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentPublishVo {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "内容类型不能为空")
    private Integer type;

    private List<String> imgUris;

    private List<String> videoUris;

    private List<String> fileUris;

    private List<String> linkUris;

    private String content;

    private Long topicId;
}
