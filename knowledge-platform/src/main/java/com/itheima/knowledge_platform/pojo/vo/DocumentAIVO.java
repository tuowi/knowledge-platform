package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentAIVO {
    private Long id;
    private String title;
    private String content; // 文档地址
    private String statusName; // 0=草稿，1=已发布，2=已归档
    private String auditStatusName; // 0=编辑中，1=待审核，2=已通过，3=已驳回
    private String visibilityName; // 0=私有，1=团队内，2=公开
    private Long authorId;
    private Long teamId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;

    private String teamName;
    private String authorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
