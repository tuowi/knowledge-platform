package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页返回的单个文档列表数据")
public class DocumentPageVO {

    @Schema(description = "文档主键")
    private Long id;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "文档版本")
    private Integer version;

    @Schema(description = "文档状态")
    private Integer staus;

    @Schema(description = "可见范围")
    private Integer visibility;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者账号")
    private String authorName;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer favoriteCount;

    @Schema(description = "创建时间")
    //TODO 测试yaml时间格式化，注释这行代码
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
