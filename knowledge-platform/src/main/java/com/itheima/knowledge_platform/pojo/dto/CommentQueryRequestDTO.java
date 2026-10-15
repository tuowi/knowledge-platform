package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "评论查询请求参数")
public class CommentQueryRequestDTO implements Serializable{

    @Schema(description = "页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;

    @Schema(description = "搜索关键词，匹配评论内容", example = "好评")
    private String keyword;

    @Schema(description = "筛选指定用户的评论", example = "12345")
    private Long userId;

    @Schema(description = "筛选指定文档下的评论", example = "67890")
    private Long documentId;
}
