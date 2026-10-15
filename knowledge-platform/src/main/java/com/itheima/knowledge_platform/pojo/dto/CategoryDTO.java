package com.itheima.knowledge_platform.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CategoryDTO implements Serializable {
    private Long parentId; // 父分类ID，顶级为null
    private String name;
    private Long teamId;
    private Integer sortOrder;
}
