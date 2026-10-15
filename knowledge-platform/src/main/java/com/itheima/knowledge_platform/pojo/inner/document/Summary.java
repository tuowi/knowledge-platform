package com.itheima.knowledge_platform.pojo.inner.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "汇总统计")
public class Summary implements Serializable {
    @Schema(description = "总文档数", example = "25")
    private Integer totalDocuments;

    @Schema(description = "总点赞数", example = "128")
    private Integer totalLikes;

    @Schema(description = "总浏览量", example = "3560")
    private Integer totalViews;

    @Schema(description = "收到的总评论数", example = "67")
    private Integer totalReceivedComments;
}
