package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentAdminDetailPageVO {
    private Long id;
    private String title;
    private Integer version; // 乐观锁版本号
    private Integer status; // 0=草稿，1=已发布，2=已归档
    private String statusName;///
    private Integer auditStatus; // 0=编辑中，1=待审核，2=已通过，3=已驳回
    private String auditStatusName;///
    private Integer visibility; // 0=私有，1=团队内，2=公开
    private Long authorId;
    private String authorName;///
    private Long teamId;
    private String teamName;///
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private String rejectReason; // 驳回原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
