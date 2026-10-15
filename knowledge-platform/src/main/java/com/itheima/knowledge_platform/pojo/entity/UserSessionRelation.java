package com.itheima.knowledge_platform.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户-会话关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String sessionId;
}
