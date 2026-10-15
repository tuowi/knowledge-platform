package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队成员关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long teamId;
    private Long userId;
    private Integer teamRole; // 0=普通成员，1=团队管理员

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime joinedAt;
}
