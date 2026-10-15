package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.mapper.UploadSessionMapper;
import com.itheima.knowledge_platform.pojo.dto.InitRequestDTO;
import com.itheima.knowledge_platform.pojo.dto.MergeRequestDTO;
import com.itheima.knowledge_platform.pojo.entity.UploadSession;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.UploadSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 分块上传接口是通用的，只负责接收和组装文件
 * 它不知道这个文件是属于哪个文档的。所以需要一个单独接口来做绑定：把临时文件路径写入文档表的content字段。
 */
@Slf4j
@Tag(name = "文件上传模块")
@RestController
@RequestMapping("/user/upload/documents")
public class UploadDocumentController {

    @Autowired
    private UploadSessionService uploadSessionService;

    @Autowired
    private UploadSessionMapper uploadSessionMapper;

    // 初始化
    @Operation(summary = "文件上传初始化")
    @PostMapping("/init")
    public Result<?> init(@RequestBody InitRequestDTO req) {
        String uploadId = uploadSessionService.initUpload(
                req.getDocumentId(),
                req.getFileName(),
                req.getTotalChunks()
        );
        return Result.success(Map.of("uploadId", uploadId));
    }

    // 上传分块
    @Operation(summary = "上传分块")
    @PostMapping("/chunk")
    public Result<?> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam int chunkIndex,
            @RequestParam MultipartFile chunk) throws IOException {
        uploadSessionService.uploadChunk(uploadId, chunkIndex, chunk);
        return Result.success();
    }

    // 查询进度
    @Operation(summary = "查询进度")
    @GetMapping("/progress")
    public ResponseEntity<?> getProgress(@RequestParam String uploadId) {
        Set<Integer> uploadedChunks = uploadSessionService.getProgress(uploadId);
        UploadSession session = uploadSessionMapper.selectByUploadId(uploadId);

        return ResponseEntity.ok(Map.of(
                "uploadId", uploadId,
                "uploadedChunks", uploadedChunks,
                "uploadedCount", uploadedChunks.size(),
                "totalChunks", session != null ? session.getTotalChunks() : 0,
                "status", session != null ? session.getStatus() : "not_found"
        ));
    }

    // 合并
    @Operation(summary = "合并")
    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody MergeRequestDTO req) throws IOException {
        String filePath = uploadSessionService.mergeChunks(req.getUploadId());
        return ResponseEntity.ok(Map.of("filePath", filePath));
    }

    // 取消上传（用户主动取消或网络超时后清理）
    @Operation(summary = "取消上传")
    @DeleteMapping("/{uploadId}")
    public ResponseEntity<?> cancelUpload(@PathVariable String uploadId) throws IOException {
        uploadSessionService.cancelUpload(uploadId);
        return ResponseEntity.ok(Map.of("message", "上传已取消，已清理临时文件"));
    }
}
