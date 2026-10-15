package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamVO {
    private Long id;
    private String name;
    private Long ownerUserId; // 团队队长ID
    private String ownerUserName;//队长账号
    private Integer memberCount;//成员人数
    private Integer status; // 0=待创建，1=已创建，2=已解散
    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
