package com.itheima.knowledge_platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获取文档历史版本列表的数据类型")
public class DocumentVersionVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "版本号")
    private Integer version;
}
