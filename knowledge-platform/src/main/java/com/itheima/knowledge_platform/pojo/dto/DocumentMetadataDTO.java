package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "文档元数据请求接收的数据模型")
public class DocumentMetadataDTO implements Serializable {

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "团队id")
    private Long teamId;

    @Schema(description = "文档状态")
    private Integer status;

    @Schema(description = "文档可见范围")
    private Integer visibility;

    @Schema(description = "标签id")
    private List<Long> tagIds;
}
