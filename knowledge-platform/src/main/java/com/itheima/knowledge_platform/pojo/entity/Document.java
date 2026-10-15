package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档主表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String content; // Markdown正文
    private Integer version; // 乐观锁版本号
    private Integer status; // 0=草稿，1=已发布，2=已归档
    private Integer auditStatus; // 0=编辑中，1=待审核，2=已通过，3=已驳回
    private Integer visibility; // 0=私有，1=团队内，2=公开
    private Long authorId;
    private Long categoryId;
    private Long teamId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private String rejectReason; // 驳回原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}