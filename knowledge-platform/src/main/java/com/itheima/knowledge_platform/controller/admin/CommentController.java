package com.itheima.knowledge_platform.controller.admin;

import com.itheima.knowledge_platform.pojo.dto.CommentQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.vo.CommentAdminVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "评论管控模块")
@RequestMapping("/admin/comments")
@RestController("adminCommentController")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取所有评论列表
     * 扁平列表
     * page=1&size=10&keyword=string&userId=1&documentId=1
     */
    @Operation(summary = "获取所有评论列表")
    @GetMapping
    public Result<PageResult<CommentAdminVO>> page(@Valid CommentQueryRequestDTO commentQueryRequestDTO) {
        log.info("获取评论列表");
        PageResult<CommentAdminVO> result = commentService.queryAdminPage(commentQueryRequestDTO);
        return Result.success(result);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除评论：{}", id);
        commentService.deleteAdmin(id);
        return Result.success();
    }

    /**
     * 批量删除评论
     */
    @Operation(summary = "批量删除评论")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(List<Long> commentIds) {
        log.info("批量删除评论:{}", commentIds);
        commentService.deleteAdminBatch(commentIds);
        return Result.success();
    }
}
