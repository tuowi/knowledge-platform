package com.itheima.knowledge_platform.pojo.inner.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "按文档状态统计")
public class ByStatus implements Serializable {
    @Schema(description = "草稿数", example = "5")
    private Integer draft;

    @Schema(description = "已发布数", example = "18")
    private Integer published;

    @Schema(description = "已归档数", example = "2")
    private Integer archived;
}
