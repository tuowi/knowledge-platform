package com.itheima.knowledge_platform.pojo.inner.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "按可见范围统计")
public class ByVisibility implements Serializable {
    @Schema(description = "私有数", example = "3")
    private Integer privateCount;

    @Schema(description = "团队内数", example = "10")
    private Integer team;

    @Schema(description = "公开数", example = "12")
    private Integer publicCount;
}