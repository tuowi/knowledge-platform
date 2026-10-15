package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档版本表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long documentId;
    private Integer version;
    private String content;
    private Long editorId;
    private String changeRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}