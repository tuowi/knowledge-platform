package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.entity.Tags;
import com.itheima.knowledge_platform.pojo.vo.TagsVO;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "标签管理模块")
@RequestMapping("/user/tags")
@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取标签列表
     */
    @Operation(summary = "获取标签列表")
    @GetMapping
    public Result<List<Tags>> list(Long teamId) {
        log.info("获取标签列表:{}", teamId);
        List<Tags> tags = tagService.list(teamId);
        return Result.success(tags);
    }

    /**
     *  创建标签
     */
    @Operation(summary = " 创建标签")
    @PostMapping
    public Result<TagsVO> create(String name, Long teamId){
        log.info("创建标签：{}，{}",name,teamId);
        TagsVO tagsVO=tagService.create(name,teamId);
        return Result.success(tagsVO);
    }

    /**
     * 删除标签
     */
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        log.info("删除标签：{}",id);
        tagService.delete(id);
        return Result.success();
    }
}
