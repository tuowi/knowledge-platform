package com.itheima.knowledge_platform.service.Impl;

import com.itheima.knowledge_platform.exception.ChunkSpillException;
import com.itheima.knowledge_platform.mapper.UploadSessionMapper;
import com.itheima.knowledge_platform.pojo.entity.UploadSession;
import com.itheima.knowledge_platform.properties.ChunkProperties;
import com.itheima.knowledge_platform.properties.FilePathProperties;
import com.itheima.knowledge_platform.service.UploadSessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@Slf4j
@Service
public class UploadSessionServiceImpl implements UploadSessionService {

    @Autowired
    private UploadSessionMapper sessionMapper;

    @Autowired
    private FilePathProperties filePathProperties;

    @Autowired
    private ChunkProperties chunkProperties;

    // 初始化：创建上传会话
    @Transactional
    @Override
     public String initUpload(Long documentId, String fileName, int totalChunks){
        String uploadId = UUID.randomUUID().toString();

        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setDocumentId(documentId);
        session.setTotalChunks(totalChunks);
        session.setUploadedChunkList(new ArrayList<>());  // 空列表
        session.setStatus("uploading");

        sessionMapper.insert(session);

        // 创建临时目录存放分块文件
        String tempDir = filePathProperties.getTempPath() + uploadId;
        new File(tempDir).mkdirs();

        return uploadId;
    }

    // 上传分块：记录哪个块已上传
    @Override
    public void uploadChunk(String uploadId, int chunkIndex, MultipartFile chunk) throws IOException {

        Long chunkSize = chunk.getSize();
        Long maxChunkSize=chunkProperties.getMaxChunkSize();

        if (chunkSize > maxChunkSize) {
            String errorMsg = String.format(
                    "分块大小超过限制！当前大小: %d 字节 (%.2f MB)，最大允许: %d 字节 (%.2f MB)",
                    chunkSize, chunkSize / (1024.0 * 1024),
                    maxChunkSize, maxChunkSize / (1024.0 * 1024)
            );
            log.error(errorMsg);
            throw new ChunkSpillException(errorMsg);
        }

        log.info("上传分块 {}，大小: {} 字节 ({} MB)，uploadId: {}",
                chunkIndex, chunkSize, chunkSize / (1024.0 * 1024), uploadId);

        // 1. 保存分块文件到磁盘
        String chunkPath = filePathProperties.getTempPath() + uploadId + "/chunk_" + chunkIndex;
        chunk.transferTo(new File(chunkPath));

        // 2. 更新数据库：记录这个块已上传（关键！）
        UploadSession session = sessionMapper.selectByUploadId(uploadId);
        if (session == null) {
            throw new RuntimeException("上传会话不存在");
        }

        // 获取当前已上传的块列表
        List<Integer> uploadedChunks = session.getUploadedChunkList();
        if (!uploadedChunks.contains(chunkIndex)) {
            uploadedChunks.add(chunkIndex);
        }

        // 重新设置并更新数据库
        session.setUploadedChunkList(uploadedChunks);
        sessionMapper.update(session);
    }

    // 查询进度：返回已上传的块列表
    @Override
    public Set<Integer> getProgress(String uploadId) {
        UploadSession session = sessionMapper.selectByUploadId(uploadId);
        if (session == null) {
            return new HashSet<>();
        }
        return new HashSet<>(session.getUploadedChunkList());
    }


    // 合并：所有块上传完成后，合并文件
    @Transactional
    @Override
    public String mergeChunks(String uploadId) throws IOException {
        // 1. 查询会话
        UploadSession session = sessionMapper.selectByUploadId(uploadId);
        if (session == null) {
            throw new RuntimeException("上传会话不存在");
        }

        int totalChunks = session.getTotalChunks();
        List<Integer> uploadedChunks = session.getUploadedChunkList();

        // 2. 校验完整性
        if (uploadedChunks.size() != totalChunks) {
            throw new RuntimeException("还有分块未上传，已完成：" + uploadedChunks.size() + "/" + totalChunks);
        }

        // 3. 合并文件
        String finalPath = filePathProperties.getMergePath() + session.getDocumentId() + "/content_temp.md";
        File finalFile = new File(finalPath);
        finalFile.getParentFile().mkdirs();  // 确保目录存在

        try (OutputStream out = new FileOutputStream(finalFile)) {
            for (int i = 0; i < totalChunks; i++) {
                String chunkPath = filePathProperties.getTempPath() + uploadId + "/chunk_" + i;
                byte[] data = Files.readAllBytes(Paths.get(chunkPath));
                out.write(data);
            }
        }

        // 4. 更新文档表的content字段
        //documentService.updateContentPath(session.getDocumentId(), finalPath);

        // 5. 清理上传会话（关键！）
        cleanUploadSession(uploadId);

        return finalPath;
    }

    // 清理上传会话：删除数据库记录 + 删除临时文件
    private void cleanUploadSession(String uploadId) throws IOException {
        // 5.1 删除数据库记录
        sessionMapper.deleteByUploadId(uploadId);

        // 5.2 删除临时文件目录
        File tempDir = new File(filePathProperties.getTempPath() + uploadId);
        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);  // 递归删除整个目录
            // 或者手动递归删除：
            // deleteDirectory(tempDir);
        }

        log.info("上传会话清理完成: {}", uploadId);
    }

    // 手动删除目录的辅助方法
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    // 取消上传：用户主动取消
    @Override
    public void cancelUpload(String uploadId) throws IOException {
        UploadSession session = sessionMapper.selectByUploadId(uploadId);
        if (session != null) {
            cleanUploadSession(uploadId);
        }
    }
}
