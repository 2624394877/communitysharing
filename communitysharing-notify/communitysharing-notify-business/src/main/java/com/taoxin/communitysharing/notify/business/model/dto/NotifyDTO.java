package com.taoxin.communitysharing.notify.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotifyDTO {
    private Long userId; // 接收通知的用户ID
    private String content; // 通知内容
    private String title; // 通知标题
}
