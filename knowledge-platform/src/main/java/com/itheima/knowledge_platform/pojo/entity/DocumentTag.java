package com.itheima.knowledge_platform.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文档-标签关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long documentId;
    private Long tagId;
}
