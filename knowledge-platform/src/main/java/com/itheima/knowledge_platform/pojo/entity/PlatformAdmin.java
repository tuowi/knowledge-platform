package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台管理员表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String adminAccount;
    private String adminPwd;
    private Integer adminType; // 0=子管理员，1=超级管理员
    private Integer status; // 0=禁用，1=启用

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}