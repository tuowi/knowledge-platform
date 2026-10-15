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
public class TeamPageVO {
    private Long id;
    private String name;
    private Long ownerUserId; // 团队队长ID
    private String ownerUserName;//队长账号
    private Integer memberCount;//成员人数
    private Integer status; // 0=待创建，1=已创建，2=已解散
    private String statusName;

    private Integer teamRole; // 0=普通成员，1=团队管理员
    private String teamRoleName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
