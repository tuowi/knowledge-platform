package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分块传输初始化数据模型")
public class InitRequestDTO implements Serializable {

    @Schema(description = "文档id",example = "1")
    private Long documentId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "分块数目")
    private Integer totalChunks;
}
