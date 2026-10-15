package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.dto.CategoryDTO;
import com.itheima.knowledge_platform.pojo.entity.Category;
import com.itheima.knowledge_platform.pojo.vo.CategoryTreeVO;

import java.util.List;

public interface CategoryService {

    /**
     * 获取分类树
     * @param teamId
     * @return
     */
    List<CategoryTreeVO> tree(Long teamId);

    /**
     * 创建分类
     *
     * @param categoryDTO
     * @return
     */
    Category create(CategoryDTO categoryDTO);

    /**
     * 更新分类
     * @param id
     * @param categoryDTO
     * @return
     */
    Category update(Long id, CategoryDTO categoryDTO);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);
}
