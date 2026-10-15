package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "接收请求的文档数据模型")
public class DocumentDTO implements Serializable {

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "所属分类id")
    private Long categoryId;

    @Schema(description = "团队id")
    private Long teamId;

    @Schema(description = "文档基础状态")
    private Integer status;

    @Schema(description = "可见范围")
    private Integer visibility;

    @Schema(description = "标签id列表")
    private List<Long> tagIds;
}
