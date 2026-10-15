package com.itheima.knowledge_platform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReviewAdminVO {
    private Long id;
    private Integer auditStatus;
    private String rejectReason;
}
