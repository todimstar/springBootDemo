package com.liu.springbootdemo.POJO.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核队列列表项 VO
 * 展示：内容摘要、作者、发布时间、风险分、命中规则摘要
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationQueueVO {
    private Long id;
    private Long targetId;
    private String targetType;
    private Long authorId;
    private String authorName;
    private String contentSummary;
    private int riskScore;
    private String hitRules;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
