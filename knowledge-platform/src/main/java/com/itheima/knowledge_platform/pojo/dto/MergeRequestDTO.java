package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "合并文件数据的数据模型")
public class MergeRequestDTO implements Serializable {

    @Schema(description = "上传id")
    private String uploadId;

    @Schema(description = "json字符串")
    private String uploadedChunks;

    @Schema(description = "已上传块数")
    private Integer uploadChunks;

    @Schema(description = "分块块数")
    private Integer totalChunks;

    @Schema(description = "是否上传结束")
    private String status;
}
