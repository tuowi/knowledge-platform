package com.itheima.knowledge_platform.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeVO {
    private Long id;
    private Long parentId; // 父分类ID，顶级为null
    private String name;
    private Integer sortOrder;

    // 子分类
    private List<CategoryTreeVO> children = new ArrayList<>();
}
