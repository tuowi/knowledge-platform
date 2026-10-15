package com.itheima.knowledge_platform.pojo.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UploadSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uploadId;
    private Long documentId;
    private Integer totalChunks;
    private String uploadedChunks;  // JSON字符串
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    // 辅助方法：方便操作JSON
    public List<Integer> getUploadedChunkList() {
        if (uploadedChunks == null || uploadedChunks.isEmpty()) {
            return new ArrayList<>();
        }
        // 使用阿里巴巴FastJson 或 Jackson
        return JSON.parseArray(uploadedChunks, Integer.class);
    }

    public void setUploadedChunkList(List<Integer> chunks) {
        this.uploadedChunks = JSON.toJSONString(chunks);
    }
}
