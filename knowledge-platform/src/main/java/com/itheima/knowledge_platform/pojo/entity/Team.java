package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long ownerUserId; // 团队队长ID
    private Integer status; // 0=待创建，1=已创建，2=已解散

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}