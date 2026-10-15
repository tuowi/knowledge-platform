package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.dto.CommentDTO;
import com.itheima.knowledge_platform.pojo.vo.CommentVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "评论模块")
@RequestMapping("/user/comments")
@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论
     */
    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Map<String, Long>> publish(@RequestBody CommentDTO commentDTO) {
        log.info("发表评论：{}", commentDTO);
        Long id = commentService.publish(commentDTO);
        return Result.success(Map.of("id", id));
    }

    /**
     * 获取文档评论列表
     */
    @Operation(summary = "获取文档评论列表")
    @GetMapping("/documents/{documentId}")
    public Result<?> page(@PathVariable Long documentId,
                          @RequestParam(value = "page", defaultValue = "1")
                          Integer page,
                          @RequestParam(value = "size", defaultValue = "10")
                          Integer size
    ) {
        log.info("获取文档评论列表:{}", documentId);
        PageResult<CommentVO> result = commentService.getCommentListByDocument(documentId, page, size);
        return Result.success(result);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除评论:{}", id);
        commentService.delete(id);
        return Result.success();
    }
}
