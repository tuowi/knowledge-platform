package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.exception.DocumentErrorException;
import com.itheima.knowledge_platform.pojo.dto.DocumentDTO;
import com.itheima.knowledge_platform.pojo.dto.DocumentMetadataDTO;
import com.itheima.knowledge_platform.pojo.dto.DocumentQueryPageDTO;
import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Tag(name = "个人文档管理模块")
@RequestMapping("/user/documents")
@RestController("userDocumentController")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * 创建文档
     */
    @Operation(summary = "创建文档")
    @PostMapping
    public Result<DocumentVO> save(@RequestBody DocumentDTO documentDTO) {
        log.info("用户创建新文档：{}", documentDTO);
        DocumentVO documentVO = documentService.save(documentDTO);
        return Result.success(documentVO);
    }

    /**
     * 上传/更新文档（上传完成后）
     * 更新数据库-----文档表，文档版本表
     * 用户点击保存时触发
     */
    @Operation(summary = "关联内容到文档（上传完成后）")
    @PostMapping("/{id}/content")
    public Result<DocumentConnectVO> relevance(@PathVariable Long id, String content) throws IOException {
        log.info("文档上传后关联数据库：{}，路径：{}", id, content);
        DocumentConnectVO documentConnectVO = documentService.connect(id, content);
        return Result.success(documentConnectVO);
    }

    /**
     * 更新文档元数据
     */
    @Operation(summary = "更新文档元数据")
    @PutMapping("/{id}")
    public Result<Void> editMetadata(@PathVariable Long id, @RequestBody DocumentMetadataDTO documentMetadataDTO) {
        log.info("更新文档元数据：{}", id);
        documentService.editMetadata(id, documentMetadataDTO);
        return Result.success();
    }

    /**
     * 删除文档
     * 注意：只能删除草稿状态的文档，已发布的文档需要先归档
     */
    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        log.info("删除文档：{}", id);
        documentService.deleteDocument(id);
        return Result.success();
    }
    
    /**
     * 流式读取文档内容（SSE，按行）
     */
    @Operation(summary = "流式读取文档内容（SSE，按字节）")
    @GetMapping(value = "/{id}/content/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDocumentContent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long start,
            @RequestParam(defaultValue = "4096") Integer chunkSize,
            @RequestParam(defaultValue = "fast") String speed,
            HttpServletResponse response
    ) {
        log.info("流式读取文档内容（SSE，按字节）");

        // 1. 权限校验：用户是否有权限查看此文档
        Long currentUserId = BaseContext.getCurrentUserId();
        Boolean isReadable = documentService.hasReadPermission(id, currentUserId);

        if (!isReadable){
            throw new DocumentErrorException(MessageConstant.NO_PERMISSION_TO_READ_DOCUMENT);
        }

        // 2. 禁用缓冲nginx（重要！）
        response.setHeader("X-Accel-Buffering", "no");

        // 3. 创建SSE发射器，超时时间5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 4. 异步执行流式输出
        documentService.streamContentByLine(id, start, chunkSize, speed, emitter);

        return emitter;
    }

    /**
     * 获取文档列表（不包含内容）
     */
    @Operation(summary = "获取文档列表")
    @GetMapping
    public Result<PageResult<DocumentPageVO>> list(DocumentQueryPageDTO documentQueryPageDTO) {
        log.info("获取文档列表");
        PageResult<DocumentPageVO> pageResult = documentService.page(documentQueryPageDTO);
        return Result.success(pageResult);
    }

    /**
     * 发布文档
     * 草稿→发布
     * 编辑中-》待审核
     */
    @Operation(summary = "发布文档")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("发布文档id:{}", id);
        documentService.publish(id);
        return Result.success();
    }

    /**
     * 归档文档
     * 只有已发布已通过的才能归档
     */
    @Operation(summary = "归档文档")
    @PostMapping("/{id}/archive")
    public Result<?> archive(@PathVariable Long id) {
        log.info("归档文档：{}", id);
        documentService.archive(id);
        return Result.success();
    }

    /**
     * 点赞/取消点赞文档
     */
    @Operation(summary = "点赞/取消点赞文档")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        log.info("点赞/取消点赞文档:{}", id);
        documentService.like(id);
        return Result.success();
    }

    /**
     * 获取文档历史版本列表
     */
    @Operation(summary = "获取文档历史版本列表")
    @GetMapping("/{id}/version")
    public Result<List<DocumentVersionListPageVO>> version(@PathVariable Long id) {
        log.info("获取文档历史版本列表:{}", id);
        List<DocumentVersionListPageVO> list = documentService.version(id);
        return Result.success(list);
    }

    /**
     * TODO 版本对比
     */

    /**
     * 版本回滚
     */
    @Operation(summary = "版本回滚")
    @PostMapping("/{id}/version/rollback")
    public Result<Void> rollback(@PathVariable Long id, Integer version) throws IOException {
        log.info("版本回滚：文档id:{},版本号：{}", id, version);
        documentService.rollback(id, version);
        return Result.success();
    }
}
