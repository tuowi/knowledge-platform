package com.itheima.knowledge_platform.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentReviewAdminDTO {
    /**
     * 审核状态：1=待审核，2=已通过，3=已驳回
     */
    @Min(value = 2, message = "审核状态最小值为2")
    @Max(value = 3, message = "审核状态最大值是3")
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    private String rejectReason;
}
