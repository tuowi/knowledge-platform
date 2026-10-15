package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "文档列表分页展示的数据模型")
public class DocumentQueryPageDTO implements Serializable {

    @Schema(description = "页码",example = "1")
    private Integer page;

    @Schema(description = "每页大小",example = "10")
    private Integer size;

    @Schema(description = "文档状态")
    private Integer status;

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "标签id")
    private List<Long> tagIds;

    @Schema(description = "团队id")
    private Long teamId;

    @Schema(description = "作者id")
    private Long authorId;

    @Schema(description = "可见范围",example = "0=私有，1=团队内，2=公开")
    private Integer visibility;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
