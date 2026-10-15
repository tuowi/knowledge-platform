package com.itheima.knowledge_platform.pojo.inner.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户统计数据")
public class UserStatistics implements Serializable {
    @Schema(description = "总文档数", example = "25")
    private Integer totalDocuments;

    @Schema(description = "已发布文档数", example = "18")
    private Integer publishedDocuments;

    @Schema(description = "草稿文档数", example = "5")
    private Integer draftDocuments;

    @Schema(description = "归档文档数", example = "2")
    private Integer archivedDocuments;

    @Schema(description = "总收藏数", example = "12")
    private Integer totalFavorites;

    @Schema(description = "总评论数", example = "45")
    private Integer totalComments;

    @Schema(description = "总团队数", example = "2")
    private Integer totalTeams;
}

