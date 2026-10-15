package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户偏好表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String prefKey;
    private String prefValue; // JSON格式偏好数据

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
