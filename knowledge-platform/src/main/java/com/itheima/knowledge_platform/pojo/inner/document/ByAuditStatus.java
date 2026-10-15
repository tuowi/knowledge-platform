package com.itheima.knowledge_platform.pojo.inner.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "按审核状态统计")
public class ByAuditStatus implements Serializable {
    @Schema(description = "编辑中数",example = "2")
    private Integer editing;

    @Schema(description = "待审核数", example = "2")
    private Integer pending;

    @Schema(description = "已通过数", example = "16")
    private Integer approved;

    @Schema(description = "已驳回数", example = "0")
    private Integer rejected;
}
