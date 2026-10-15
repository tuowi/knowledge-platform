package com.itheima.knowledge_platform.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public interface UploadSessionService {

    // 初始化：创建上传会话
    public String initUpload(Long documentId, String fileName, int totalChunks);

    // 上传分块：记录哪个块已上传
    public void uploadChunk(String uploadId, int chunkIndex, MultipartFile chunk) throws IOException;

    // 查询进度：返回已上传的块列表
    public Set<Integer> getProgress(String uploadId);

    // 合并：所有块上传完成后，合并文件
    public String mergeChunks(String uploadId) throws IOException;

    // 取消上传：用户主动取消
    public void cancelUpload(String uploadId) throws IOException;
}
