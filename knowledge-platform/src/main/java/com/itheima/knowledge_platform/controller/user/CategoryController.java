package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.dto.CategoryDTO;
import com.itheima.knowledge_platform.pojo.entity.Category;
import com.itheima.knowledge_platform.pojo.vo.CategoryTreeVO;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "分类管理模块")
@RequestMapping("/user/categories")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类树
     */
    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree(Long teamId) {
        log.info("获取分类树：{}", teamId);
        List<CategoryTreeVO> categoryTreeVO = categoryService.tree(teamId);
        return Result.success(categoryTreeVO);
    }

    /**
     * 创建分类
     */
    @Operation(summary = "创建分类")
    @PostMapping
    public Result<?> create(@RequestBody CategoryDTO categoryDTO) {
        log.info("创建分类：{}", categoryDTO);
        Category category = categoryService.create(categoryDTO);
        return Result.success(category);
    }

    /**
     * 更新分类
     * 核心功能：修改分类的名称、排序序号等基本信息
     */
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id,CategoryDTO categoryDTO){
        log.info("更新分类：{}",id);
        Category category=categoryService.update(id,categoryDTO);
        return Result.success(category);
    }

    /**
     * 删除分类
     * 注意：只能删除没有子分类和文档的分类
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        log.info("删除分类：{}",id);
        categoryService.delete(id);
        return Result.success();
    }
}
