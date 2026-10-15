package com.itheima.knowledge_platform.pojo.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tags implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long teamId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}