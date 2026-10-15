package com.itheima.knowledge_platform.controller.admin;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.exception.PlatformAdminErrorException;
import com.itheima.knowledge_platform.mapper.PlatformAdminMapper;
import com.itheima.knowledge_platform.pojo.dto.DocumentAdminDetailPageDTO;
import com.itheima.knowledge_platform.pojo.dto.DocumentReviewAdminDTO;
import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import com.itheima.knowledge_platform.pojo.vo.DocumentAdminDetailPageVO;
import com.itheima.knowledge_platform.pojo.vo.DocumentAdminVO;
import com.itheima.knowledge_platform.pojo.vo.DocumentReviewAdminVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Tag(name = "文档审核模块")
@RequestMapping("/admin/documents")
@RestController("adminDocumentController")
public class DocumentController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    /**
     * 获取待审核文档列表
     */
    @Operation(summary = "获取待审核文档列表")
    @GetMapping("/pending")
    public Result<PageResult<DocumentAdminVO>> reviewPage(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value = "title", required = false) String title
    ) {
        log.info("获取待审核文档列表:{}", title);
        PageResult<DocumentAdminVO> result = documentService.adminReviewPage(page, size, title);
        return Result.success(result);
    }

    /**
     * 获取所有文档列表
     */
    @Operation(summary = "获取所有文档列表")
    @GetMapping
    public Result<PageResult<DocumentAdminDetailPageVO>> page(@Valid DocumentAdminDetailPageDTO documentAdminDetailPageDTO) {
        log.info("获取所有文档列表");
        PageResult<DocumentAdminDetailPageVO> result = documentService.adminPage(documentAdminDetailPageDTO);
        return Result.success(result);
    }

    /**
     * 审核文档
     */
    @Operation(summary = "审核文档")
    @PutMapping("/{id}/audit")
    public Result<DocumentReviewAdminVO> documentReview(@PathVariable Long id, @Valid @RequestBody DocumentReviewAdminDTO documentReviewAdminDTO) {
        log.info("审核文档：{},{}", id, documentReviewAdminDTO);
        DocumentReviewAdminVO result = documentService.adminReview(id, documentReviewAdminDTO);
        return Result.success(result);
    }

    /**
     * 校验 获取文档详情（管理员视角）
     */
    @Operation(summary = "获取文档详情（管理员视角）")
    @GetMapping(value = "/{id}/content/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter documentDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long start,
            @RequestParam(defaultValue = "4096") Integer chunkSize,
            @RequestParam(defaultValue = "fast") String speed,
            HttpServletResponse response
    ) {
        log.info("获取文档详情（管理员视角）:{}", id);
        // 1. 权限校验：当前用户必须是管理员
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new PlatformAdminErrorException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 禁用缓冲
        response.setHeader("X-Accel-Buffering", "no");

        // 3. 创建SSE发射器
        SseEmitter emitter = new SseEmitter(300000L);

        // 4. 异步执行流式输出（复用同一个Service）
        documentService.streamContentByByte(id, start, chunkSize, speed, emitter);

        return emitter;
    }
}
