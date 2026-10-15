package com.itheima.knowledge_platform.task;

import com.itheima.knowledge_platform.mapper.UploadSessionMapper;
import com.itheima.knowledge_platform.properties.FilePathProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class UploadCleanupTask {

    @Autowired
    private UploadSessionMapper sessionMapper;

    @Autowired
    private FilePathProperties filePathProperties;

    // 每天凌晨3点执行，清理过期会话
    //TODO 进行测试
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredSessions() throws IOException {
        log.info("处理过期缓存文件块：{}", LocalDateTime.now());
        // 1. 先查询所有过期的会话（需要先查出uploadId列表）
        List<String> expiredIds = sessionMapper.selectExpiredUploadIds();

        // 2. 删除临时文件
        for (String uploadId : expiredIds) {
            File tempDir = new File(filePathProperties.getTempPath() + uploadId);
            if (tempDir.exists()) {
                FileUtils.deleteDirectory(tempDir);
            }
        }

        // 3. 删除数据库记录
        int deleted = sessionMapper.deleteExpiredSessions();
        log.info("清理了 {} 个过期上传会话", deleted);
    }
}
